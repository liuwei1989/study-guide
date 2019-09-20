package com.gitbook.algorithm.search;


import javafx.util.Pair;

import java.util.*;

/**
 * @author liuwei56
 * @version 2019/9/17 4:58 PM
 * @description 功能描述
 * @see
 * @since 1.0
 */
public class SearchDemo {

    public static void main(String[] args) {
        int[][] grids = new int[][]{
                {1, 1, 0, 1},
                {1, 0, 1, 0},
                {1, 1, 1, 1},
                {1, 0, 1, 1}
        };
        System.out.println(SearchDemo.minPathLength(grids, 2, 3));
        System.out.println(SearchDemo.numSquares(12));

        DFS dfs = new SearchDemo.DFS();
        grids = new int[][]
                {{0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0},
                        {0, 1, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0}};
        System.out.println(dfs.maxAreaOfIsland(grids));
        char[][] grids2 = new char[][]{{'1', '1', '0', '0', '0'}, {'1', '1', '0', '0', '0'}, {'0', '0', '1', '0', '0'}, {'0', '0', '0', '1', '1'}};
        System.out.println(dfs.numIslands(grids2));

        grids = new int[][]{
                {1, 1, 0},
                {1, 1, 0},
                {0, 0, 1}
        };
        System.out.println(dfs.findCircleNum(grids));

        DFS2 dfs2 = new DFS2();
        grids2 = new char[][]{
                {'X', 'X', 'X', 'X'},
                {'X', 'O', 'O', 'X'},
                {'X', 'X', 'O', 'X'},
                {'X', 'O', 'X', 'X'}};
        dfs2.solve(grids2);
        for (char[] grid : grids2) {
            System.out.println(Arrays.toString(grid));
        }

        DFS3 dfs3 = new DFS3();
        List<int[]> list = dfs3.pacificAtlantic(grids);
        for (int[] l : list) {
            System.out.println(Arrays.toString(l));
        }
    }

    public static int minPathLength(int[][] grids, int tr, int tc) {
        final int[][] direction = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        int row = grids[0].length, col = grids.length;
        Queue<Pair<Integer, Integer>> pathQueue = new LinkedList<>();
        pathQueue.add(new Pair<>(0, 0));
        int pathLength = 0;
        while (!pathQueue.isEmpty()) {
            pathLength++;
            int size = pathQueue.size();
            while (size-- > 0) {
                Pair<Integer, Integer> poll = pathQueue.poll();
                Integer pr = poll.getKey();
                Integer pc = poll.getValue();
                grids[pr][pc] = 0;
                for (int[] d : direction) {
                    int cr = pr + d[0], cc = pc + d[1];
                    if (cr < 0 || cr >= row || cc < 0 || cc >= col || grids[cr][cc] == 0) {
                        continue;
                    }
                    if (cr == tr && cc == tc) {
                        return pathLength;
                    }
                    pathQueue.add(new Pair<>(cr, cc));
                }
            }
        }
        return -1;
    }

    /**
     * [279. Perfect Squares (Medium)](https://leetcode.com/problems/perfect-squares/description/)
     *
     * @param n
     * @return
     */
    public static int numSquares(int n) {
        List<Integer> squares = generateSquares(n);
        Queue<Integer> queue = new LinkedList<>();
        queue.add(n);
        boolean[] marked = new boolean[n + 1];
        marked[n] = true;
        int level = 0;
        while (!queue.isEmpty()) {
            level++;
            int size = queue.size();
            while (size-- > 0) {
                Integer poll = queue.poll();
                for (Integer square : squares) {
                    int next = poll - square;
                    if (next < 0) {
                        break;
                    }
                    if (next == 0) {
                        return level;
                    }
                    if (marked[next]) {
                        continue;
                    }
                    marked[next] = true;
                    queue.add(next);
                }
            }
        }
        return n;
    }

    private static List<Integer> generateSquares(int n) {
        List<Integer> squares = new ArrayList<>();
        int square = 1;
        int diff = 3;
        while (square <= n) {
            squares.add(square);
            square += diff;
            diff += 2;
        }
        return squares;
    }

    private static class DFS {
        private int m, n;
        private int[][] direction = new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        /**
         * [695. Max Area of Island (Medium)](https://leetcode.com/problems/max-area-of-island/description/)
         *
         * @param grids
         * @return
         */
        public int maxAreaOfIsland(int[][] grids) {
            if (grids == null || grids.length == 0) {
                return 0;
            }
            int max = 0;
            m = grids.length;
            n = grids[0].length;
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < m; j++) {
                    max = Math.max(max, dfs(grids, i, j));
                }
            }
            return max;
        }

        private int dfs(int[][] grids, int r, int c) {
            if (r < 0 || r >= m || c < 0 || c >= n || grids[r][c] == 0) {
                return 0;
            }
            grids[r][c] = 0;
            int area = 1;
            for (int[] d : direction) {
                area += dfs(grids, r + d[0], c + d[1]);
            }
            return area;
        }

        /**
         * [200. Number of Islands (Medium)](https://leetcode.com/problems/number-of-islands/description/)
         *
         * @param grids
         * @return
         */
        public int numIslands(char[][] grids) {
            if (grids == null || grids.length == 0) {
                return 0;
            }
            m = grids.length;
            n = grids[0].length;
            int nums = 1;
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    if (grids[i][j] != '0') {
                        dfs(grids, i, j);
                        nums++;
                    }
                }
            }
            return nums;
        }

        private void dfs(char[][] grids, int r, int c) {
            if (r < 0 || r >= m || c < 0 || c >= n || grids[r][c] == '0') {
                return;
            }
            grids[r][c] = '0';
            for (int[] d : direction) {
                dfs(grids, r + d[0], c + d[1]);
            }
        }

        /**
         * [547. Friend Circles (Medium)](https://leetcode.com/problems/friend-circles/description/)
         *
         * @param grids
         * @return
         */
        public int findCircleNum(int[][] grids) {
            m = grids.length;
            int circleNum = 0;
            boolean[] hasVisited = new boolean[m];
            for (int i = 0; i < m; i++) {
                if (!hasVisited[i]) {
                    dfs(grids, i, hasVisited);
                    circleNum++;
                }
            }
            return circleNum;
        }

        private void dfs(int[][] grids, int m, boolean[] hasVisited) {
            hasVisited[m] = true;
            for (int i = 0; i < m; i++) {
                if (grids[m][i] == 1 && !hasVisited[i]) {
                    dfs(grids, i, hasVisited);
                }
            }
        }

    }

    private static class DFS2 {
        private int m, n;
        private int[][] direction = new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}};


        /**
         * [130. Surrounded Regions (Medium)](https://leetcode.com/problems/surrounded-regions/description/)
         *
         * @param board
         */
        public void solve(char[][] board) {
            if (board == null || board.length == 0) {
                return;
            }
            m = board.length;
            n = board[0].length;
            for (int i = 0; i < m; i++) {
                dfs(board, i, 0);
                dfs(board, i, n - 1);
            }
            for (int i = 0; i < n; i++) {
                dfs(board, 0, i);
                dfs(board, m - 1, i);
            }
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    if (board[i][j] == 'T') {
                        board[i][j] = 'O';
                    } else if (board[i][j] == 'O') {
                        board[i][j] = 'X';
                    }
                }
            }
        }

        private void dfs(char[][] board, int r, int c) {
            if (r < 0 || r >= m || c < 0 || c >= n || board[r][c] != 'O') {
                return;
            }
            board[r][c] = 'T';
            for (int[] d : direction) {
                dfs(board, r + d[0], c + d[1]);
            }
        }
    }

    private static class DFS3 {
        private int m, n;
        private int[][] matrix;
        private int[][] direction = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        public List<int[]> pacificAtlantic(int[][] matrix) {
            List<int[]> ret = new ArrayList<>();
            if (matrix == null || matrix.length == 0) {
                return ret;
            }

            m = matrix.length;
            n = matrix[0].length;
            this.matrix = matrix;
            boolean[][] canReachP = new boolean[m][n];
            boolean[][] canReachA = new boolean[m][n];

            for (int i = 0; i < m; i++) {
                dfs(i, 0, canReachP);
                dfs(i, n - 1, canReachA);
            }
            for (int i = 0; i < n; i++) {
                dfs(0, i, canReachP);
                dfs(m - 1, i, canReachA);
            }

            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    if (canReachP[i][j] && canReachA[i][j]) {
                        ret.add(new int[]{i, j});
                    }
                }
            }

            return ret;
        }

        private void dfs(int r, int c, boolean[][] canReach) {
            if (canReach[r][c]) {
                return;
            }
            canReach[r][c] = true;
            for (int[] d : direction) {
                int nextR = d[0] + r;
                int nextC = d[1] + c;
                if (nextR < 0 || nextR >= m || nextC < 0 || nextC >= n
                        || matrix[r][c] > matrix[nextR][nextC]) {

                    continue;
                }
                dfs(nextR, nextC, canReach);
            }
        }
    }
}
