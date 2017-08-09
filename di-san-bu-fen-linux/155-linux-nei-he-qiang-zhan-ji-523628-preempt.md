早期的Linux核心是不可抢占的。它的调度方法是：一个进程可以通过schedule\(\)函数自愿地启动一次调度。非自愿的强制性调度只能发生在 每次从系统调用返回的前夕以及每次从中断或异常处理返回到用户空间的前夕。但是，如果在系统空间发生中断或异常是不会引起调度的。这种方式使内核实现得以 简化。但常存在下面两个问题：

* 如果这样的中断发生在内核中,本次中断返回是不会引起调度的,而要到最初使CPU从用户空间进入内核空间的那次系统调用或中断\(异常\)返回时才会发生调度。
* 另外一个问题是优先级反转。在Linux中，在核心态运行的任何操作都要优先于用户态进程，这就有可能导致优先级反转问题的出现。例如，一个低优先级的用户进程由于执行软/硬中断等原因而导致一个高优先级的任务得不到及时响应。

当前的Linux内核加入了内核抢占\(preempt\)机制。内核抢占指用户程序在执行系统调用期间可以被抢占，该进程暂时挂起，使新唤醒的高优先 级进程能够运行。这种抢占并非可以在内核中任意位置都能安全进行，比如在临界区中的代码就不能发生抢占。临界区是指同一时间内不可以有超过一个进程在其中 执行的指令序列。在Linux内核中这些部分需要用自旋锁保护。

内核抢占要求内核中所有可能为一个以上进程共享的变量和数据结构就都要通过互斥机制加以保护，或者说都要放在临界区中。在抢占式内核中，认为如果内 核不是在一个中断处理程序中，并且不在被 spinlock等互斥机制保护的临界代码中，就认为可以"安全"地进行进程切换。

Linux内核将临界代码都加了互斥机制进行保护，同时，还在运行时间过长的代码路径上插入调度检查点，打断过长的执行路径，这样，任务可快速切换进程状态，也为内核抢占做好了准备。

Linux内核抢占只有在内核正在执行例外处理程序（通常指系统调用）并且允许内核抢占时，才能进行抢占内核。禁止内核抢占的情况列出如下：

（1）内核执行中断处理例程时不允许内核抢占，中断返回时再执行内核抢占。

（2）当内核执行软中断或tasklet时，禁止内核抢占，软中断返回时再执行内核抢占。

（3）在临界区禁止内核抢占，临界区保护函数通过抢占计数宏控制抢占，计数大于0，表示禁止内核抢占。

抢占式内核实现的原理是在释放自旋锁时或从中断返回时，如果当前执行进程的 need\_resched 被标记，则进行抢占式调度。

Linux内核在线程信息结构上增加了成员preempt\_count作为内核抢占锁，为0表示可以进行内核高度，它随spinlock和 rwlock等一起加锁和解锁。线程信息结构thread\_info列出如下（在include/asm-x86/thread\_info.h中）：

```cpp
struct thread_info { 
struct task_struct *task; struct exec_domain *exec_domain; 
__u32 flags; 
__u32 status; __u32 cpu; int preempt_count; mm_segment_t addr_limit; 
struct restart_block restart_block; #ifdef CONFIG_IA32_EMULATION void __user *sysenter_return; #endif 
}; #endif
```



内核调度器的入口为preempt\_schedule\(\)，他将当前进程标记为TASK\_PREEMPTED状态再调用schedule\(\)，在TASK\_PREEMPTED状态，schedule\(\)不会将进程从运行队列中删除。

#### 内核抢占API函数

在中断或临界区代码中，线程需要关闭内核抢占，因此，互斥机制（如：自旋锁（spinlock）、RCU等）、中断代码、链表数据遍历等需要关闭内 核抢占，临界代码运行完时，需要开启内核抢占。关闭/开启内核抢占需要使用内核抢占API函数preempt\_disable和 preempt\_enable。

内核抢占API函数说明如下（在include/linux/preempt.h中）：

preempt\_enable\(\) //内核抢占计数preempt\_count减1

preempt\_disable\(\) //内核抢占计数preempt\_count加1

preempt\_enable\_no\_resched\(\)　 //内核抢占计数preempt\_count减1，但不立即抢占式调度

preempt\_check\_resched \(\) //如果必要进行调度

preempt\_count\(\) //返回抢占计数

preempt\_schedule\(\) //核抢占时的调度程序的入口点

内核抢占API函数的实现宏定义列出如下（在include/linux/preempt.h中）：

```
#define preempt_disable() / 
do { / inc_preempt_count(); / barrier(); / //加内存屏障，阻止gcc编译器对内存进行优化 } 
while (0) #define inc_preempt_count() / do { / preempt_count()++; / } 
while (0) #define preempt_count() (current_thread_info()->preempt_count)
```

  


## 内核抢占调度

Linux内核在硬中断或软中断返回时会检查执行抢占调度。分别说明如下：

（1）硬中断返回执行抢占调度

Linux内核在硬中断或出错退出时执行函数retint\_kernel，运行抢占函数，函数retint\_kernel列出如下（在arch/x86/entry\_64.S中）：

```
#ifdef CONFIG_PREEMPT ENTRY(retint_kernel) cmpl 
$0,threadinfo_preempt_count(%rcx) jnz retint_restore_args bt $TIF_NEED_RESCHED,
threadinfo_flags(%rcx) jnc retint_restore_args bt $9,EFLAGS-ARGOFFSET(%rsp) 
jnc retint_restore_args call preempt_schedule_irq jmp exit_intr #endif
```

函数preempt\_schedule\_irq是出中断上下文时内核抢占调度的入口点，该函数被调用和返回时中断应关闭，保护此函数从中断递归调用。该函数列出如下（在kernel/sched.c中）：

```
asmlinkage void 
__sched preempt_schedule_irq(void) { struct thread_info *ti = current_thread_info();   
BUG_ON(ti->preempt_count || !irqs_disabled());   
do { add_preempt_count(PREEMPT_ACTIVE); local_irq_enable(); schedule(); 
local_irq_disable(); sub_preempt_count(PREEMPT_ACTIVE);   
barrier(); } while (unlikely(test_thread_flag(TIF_NEED_RESCHED))); }
```

调度函数schedule会检测进程的 preempt\_counter 是否很大，避免普通调度时又执行内核抢占调度。

（2）软中断返回执行抢占调度

在打开页出错函数pagefault\_enable和软中断底半部开启函数local\_bh\_enable中，会调用函数 preempt\_check\_resched检查是否需要执行内核抢占。如果不是并能调度，进程才可执行内核抢占调度。函数 preempt\_check\_resched列出如下：

```
#define preempt_check_resched() / 
do { / if (unlikely(test_thread_flag(TIF_NEED_RESCHED))) / preempt_schedule(); / } while (0)

```

函数preempt\_schedule源代码与函数preempt\_schedule\_irq基本上一样，对进程进行调度，这里不再分析。

