Milo老湿在他的博文 [用JavaScript玩转游戏编程\(一\)掉宝类型概率](http://www.cnblogs.com/miloyip/archive/2010/04/21/1717109.html)中提到了游戏中按一定概率掉宝的算法，即根据给定的PDF（probability density function），在线性时间内算出对应的CDF（cumulative distribution function），然后进行取样。一个例子如下：

|  |  |  |  |  |
| :--- | :--- | :--- | :--- | :--- |
| val | 1 | 2 | 3 | 4 |
| prob | 0.1 | 0.2 | 0.3 | 0.4 |



取样的概率如表所示，那么可求出对应的CDF，即积累概率如下：

|  |  |  |  |  |
| :--- | :--- | :--- | :--- | :--- |
| val | 1 | 2 | 3 | 4 |
| cum | 0.1 | 0.3 | 0.6 | 1.0 |

实际进行取样的时候，令 p = rand\(\) 产生一个0~1的随机值，然后看 p 落在CDF哪个范围内，就取哪个值返回。如某一个 p=0.4，属于0.3~0.6，那么就取0.6对应的3作为采样值返回。这相当于每个val根据自己的概率占据了容易看到，使用该方法取样

时间复杂度为

O\(logN\)

，如果进行二分查找的话。

有没有更快的方法呢？就是我们标题里面的 Alias Method（别名方法）。

该方法构造一个别名表，每次采样时，通过两次rand\(\)来决定采样值，针对上面的PDF表，构造的Alias表如下：

|  |  |  |  |  |
| :--- | :--- | :--- | :--- | :--- |
| val | 1 | 2 | 3 | 4 |
| alias | 3 | 4 | 4 | - |
| prob | 0.4 | 0.8 | 0.6 | 1.0 |



根据该表，采样的过程如下，在第一轮我们按 1/N 的概率选择一个采样值，第二轮中根据 alias 表中的概率，看是选择该值还是其别名；如对于val=1，其在第一轮被选中的概率是 1/4=0.25，在第二轮中选择 val=1 而不是其别名（alias=3）的概率是prob=0.4；因此采样值val=1的概率为 0.25\*0.4 = 0.1，与PDF符合。读者可自行验证其它几个采样值是否与PDF相符。采用该方法的采样

复杂度为O\(1\)。

古人云：

天之道，取有余而补不足

。Alias method用的就是这个道理！

Alias table的构造复杂度为O\(n\)，java代码如下：

```
public class AliasMethod {
	private final double[] probability;
	private final int[] alias;
	private final int length;
	private final Random rand;
	
	public AliasMethod(List<Double> prob){
		this(prob,new Random());
	}
	
	public AliasMethod(List<Double> prob,Random rand){
		/* Begin by doing basic structural checks on the inputs. */
        if (prob == null || rand == null)
            throw new NullPointerException();
        if (prob.size() == 0)
            throw new IllegalArgumentException("Probability vector must be nonempty.");
        
        this.rand = rand;
        this.length = prob.size();
        this.probability = new double[length];
        this.alias = new int[length];       
        
        double[] probtemp = new double[length];
        Deque<Integer> small = new ArrayDeque<Integer>();
        Deque<Integer> large = new ArrayDeque<Integer>();
        
        /* divide elements into 2 groups by probability */
        for(int i=0;i<length;i++){
        	probtemp[i] = prob.get(i)*length;  /* initial probtemp */
        	if(probtemp[i]<1.0)
        		small.add(i);
        	else
        		large.add(i);
        }
        
        while(!small.isEmpty() && !large.isEmpty()){
        	int less = small.pop();
        	int more = large.pop();
        	probability[less] = probtemp[less];
        	alias[less] = more;
        	probtemp[more] = probtemp[more]-(1.0-probability[less]);
        	if(probtemp[more]<1.0)
        		small.add(more);
        	else
        		large.add(more);
        }
        /* At this point, everything is in one list, which means that the
         * remaining probabilities should all be 1/n.  Based on this, set them
         * appropriately.
         */
        while (!small.isEmpty())
            probability[small.pop()] = 1.0;
        while (!large.isEmpty())
            probability[large.pop()] = 1.0;       
	}
	
	/**
     * Samples a value from the underlying distribution.
     *
     */
    public int next() {
        /* Generate a fair die roll to determine which column to inspect. */
        int column = rand.nextInt(length);

        /* Generate a biased coin toss to determine which option to pick. */
        boolean coinToss = rand.nextDouble() < probability[column];

        /* Based on the outcome, return either the column or its alias. */
        return coinToss? column : alias[column];
    }
    
    public static void main(String[] argv){
    	List<Double> prob = new ArrayList<Double>();
    	prob.add(0.1);prob.add(0.2);prob.add(0.3);prob.add(0.4);   	
    	int[] cnt = new int[prob.size()];
    	AliasMethod am = new AliasMethod(prob);
    	for(int i=0;i<10000;i++){
    		cnt[am.next()]++;
    	}
    	for(int i=0;i<cnt.length;i++)
    		System.out.println(cnt[i]);
    }
}
```



