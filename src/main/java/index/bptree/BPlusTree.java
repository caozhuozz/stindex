package index.bptree;

import java.util.*;

public class BPlusTree<Key extends Comparable<? super Key>, Value> {


        /* M is the maximum number of keys in the inner node */
        private final int M;
        /* N is the maximum number of keys in the leaf node */
        private final int N;
        private Node root;


        public BPlusTree(int n) {
                this(n, n);
        }


        public BPlusTree(int m, int n) {
                M = m;
                N = n;
                this.root = new LNode();
        }


        public void clear(){
                root = new LNode();
        }


        public void insert(Key key, Value value){
                System.out.println("insert key=" + key + ", value=" + value);
                Split result = root.insert(key, value);
                if (result != null) {
                        // The old root was split into two parts.
                        // We have to create a new root pointing to them
                        INode _root = new INode();
                        _root.num = 1;
                        _root.keys[0] = result.key;
                        _root.values[0] = result.lNode;
                        _root.values[1] = result.rNode;
                        root = _root;
                }

        };


        public Value find(Key key){
                Node node = root;
                while (node instanceof BPlusTree.INode) { // need to traverse down to the leaf
                        INode inner = (INode) node;
                        int idx = inner.getLoc(key);
                        node = inner.values[idx];
                }

                //We are @ leaf after while loop
                LNode leaf = (LNode) node;
                int idx = leaf.getLoc(key);
                if (idx < leaf.num && leaf.keys[idx].equals(key)) {
                        return leaf.values[idx];
                } else {
                        return null;
                }
        };


        public void delete(Key key){
                System.out.println("Delete options is not supported!");
        };

        public void update(Key key, Value value){
                System.out.println("Update options is not supported!");
        };


        public void view(){

                Queue<Node> queue = new LinkedList<>();
                queue.add(root);

                int h = 0;
                while (!queue.isEmpty()){
                        System.out.println("h = " + h++);

                        int size = queue.size();
                        for (int i = 0; i<size; i++) {
                                Node n = queue.remove();
                                if (n instanceof BPlusTree.INode) {
                                        INode in = (INode) n;
                                        Iterator<Key> it = Arrays.stream(in.keys).iterator();
                                        while (it.hasNext()){
                                                System.out.print(it.next() + " ");
                                        }
                                        Iterator<Node> it1 = Arrays.stream(in.values).iterator();
                                        while (it1.hasNext()){
                                                queue.add(it1.next());
                                        }
                                        System.out.print(" | ");
                                } else if (n instanceof BPlusTree.LNode) {
                                        LNode ln = (LNode) n;
                                        for (int j = 0; j < ln.num; j++) {
                                                System.out.print(ln.keys[j] + " ");

                                        }
                                        System.out.print(" | ");
                                }
                        }
                        System.out.println("");
                }
        };


        abstract class Node{
                protected int num;
                protected Key[] keys;

                abstract public Split insert(Key key, Value value);

                abstract public int getLoc(Key key);

        }


        class Split{
                Key key;
                Node lNode;
                Node rNode;
                public Split(Key k, Node l, Node r){
                        key = k; lNode = l; rNode = r;
                }
        }


        class INode extends Node {

                Node[] values = new BPlusTree.Node[M+1];
                {
                        keys = (Key[]) new Comparable[M];

                }

                @Override
                public Split insert(Key key, Value value) {
                        /* Early split if node is full.
                         * This is not the canonical algorithm for B+ trees,
                         * but it is simpler and it does break the definition
                         * which might result in immature split, which might not be desired in database
                         * because additional split lead to tree's height increase by 1, thus the number of disk read
                         * so first search to the leaf, and split from bottom up is the correct approach.
                         */

                        if (this.num == N) { // Split
                                int mid = (N+1)/2;
                                int sNum = this.num - mid;
                                INode sibling = new INode();
                                sibling.num = sNum;
                                System.arraycopy(this.keys, mid, sibling.keys, 0, sNum);
                                System.arraycopy(this.values, mid, sibling.values, 0, sNum+1);

                                // set null
                                for(int i = mid; i < mid+sNum; i++){
                                        this.keys[i] = null;
                                }
                                for(int i = mid; i < mid+sNum+1; i++){
                                        this.values[i] = null;
                                }


                                this.num = mid-1;//this is important, so the middle one elevate to next depth(height), inner node's key don't repeat itself

                                // Set up the return variable
                                Split result = new Split(this.keys[mid-1],
                                        this,
                                        sibling);

                                // Now insert in the appropriate sibling
                                if (key.compareTo(result.key) < 0) {
                                        this.insertNonfull(key, value);
                                } else {
                                        sibling.insertNonfull(key, value);
                                }
                                return result;

                        } else {// No split
                                this.insertNonfull(key, value);
                                return null;
                        }
                }


                private void insertNonfull(Key key, Value value) {
                        // Simple linear search
                        int idx = getLoc(key);
                        Split result = values[idx].insert(key, value);

                        if (result != null) {
                                if (idx == num) {
                                        // Insertion at the rightmost key
                                        keys[idx] = result.key;
                                        values[idx] = result.lNode;
                                        values[idx+1] = result.rNode;
                                        num++;
                                } else {
                                        // Insertion not at the rightmost key
                                        //shift i>idx to the right
                                        System.arraycopy(keys, idx, keys, idx+1, num-idx);
                                        System.arraycopy(values, idx, values, idx+1, num-idx+1);

                                        values[idx] = result.lNode;
                                        values[idx+1] = result.rNode;
                                        keys[idx] = result.key;
                                        num++;
                                }
                        } // else the current node is not affected
                }

                public int getLoc(Key key){
                        for (int i = 0; i < num; i++){
                                if (keys[i].compareTo(key) > 0){
                                        return i;
                                }
                        }
                        return num;
                }

        }


        class LNode extends Node{
                Value[] values = (Value[]) new Object[N];
                { keys = (Key[]) new Comparable[N]; }


                @Override
                public Split insert(Key key, Value value) {
                        int i = getLoc(key);
                        if (this.num == N) {
                                int mid = (N+1)/2;
                                int sNum = this.num - mid;
                                LNode sibling = new LNode();
                                sibling.num = sNum;
                                System.arraycopy(this.keys, mid, sibling.keys, 0, sNum);
                                System.arraycopy(this.values, mid, sibling.values, 0, sNum);
                                this.num = mid;
                                if (i < mid) {
                                        this.insertNonfull(key, value, i);
                                } else {
                                        sibling.insertNonfull(key, value, i-mid);
                                }
                                BPlusTree.Split result = new BPlusTree.Split(sibling.keys[0], this, sibling);
                                return result;
                        } else {
                                this.insertNonfull(key, value, i);
                                return null;
                        }

                }

                private void insertNonfull(Key key, Value value, int idx) {
                        //if (idx < M && keys[idx].equals(key)) {
                        if (idx < num && keys[idx].equals(key)) {
                                // We are inserting a duplicate value, simply overwrite the old one
                                values[idx] = value;
                        } else {
                                // The key we are inserting is unique
                                System.arraycopy(keys, idx, keys, idx+1, num-idx);
                                System.arraycopy(values, idx, values, idx+1, num-idx);

                                keys[idx] = key;
                                values[idx] = value;
                                num++;
                        }
                }
                public int getLoc(Key key){
                        for (int i = 0; i < num; i++){
                                if (keys[i].compareTo(key) >= 0){
                                        return i;
                                }
                        }
                        return num;
                }


        }


        public static void main(String[] args) {
                BPlusTree t = new BPlusTree(15);
                Random r = new Random();

                for(int i=0; i<100000; i++){
                        int num = r.nextInt(100000);
                        t.insert(num, num);
                }


                t.view();


                System.out.println(t.find(32));



        }


}
