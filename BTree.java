/**
Nikita Zorin BS21-07 Innopolis University
*/

/* Import libraries */

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;
import java.text.SimpleDateFormat;

/* Main method */

public class Main {

    public static void main(String[] args) throws Exception{

        /* There I create rule for the date parsing */

        SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd");
        Scanner sc = new Scanner(System.in);

        /* Creation of the empty B-Tree */

        BTree tree = new BTree();

        /* Start of the data parsing */

        String s = sc.nextLine();
        int n = Integer.parseInt(s);
        Calendar date = Calendar.getInstance();
        for (int i = 0; i < n; i++) {
            s = sc.nextLine();
            String[] str = s.split(" ");

            /* Actions when making a deposit */

            if (str.length==3 && str[1].equals("DEPOSIT")){
                date.setTime(sdf.parse(str[0]));

                /*
                In this implementation I decided to use time in milliseconds as a key.
                The main problem is that .getTimeInMillis() method returns long, so
                the decision of this problem is the decreasing size of number to some integer.
                The same approach is using in the further code.
                */

                int key = Integer.parseInt(String.valueOf(date.getTimeInMillis()/86400000));
                int value = Integer.parseInt(str[2]);
                BTree.toRet p = tree.search(tree.root,key);

                /*
                In my implementation of B-Tree I wanted to avoid duplicates, so I decided to increase/decrease value,
                if such key already exist in the tree.
                The same approach is using in the withdraw case.
                */

                if (p!=null) p.x.keys[p.i].value+=value;
                else tree.add(key, value);
            }

            else if (str.length==3 && str[1].equals("WITHDRAW")){

                /* Program works the same as for Deposit, except that value is negative for Withdraw */

                date.setTime(sdf.parse(str[0]));
                int key = Integer.parseInt(String.valueOf(date.getTimeInMillis()/86400000));
                int value = -Integer.parseInt(str[2]);
                BTree.toRet p = tree.search(tree.root,key);

                if (p!=null) p.x.keys[p.i].value+=value;
                else tree.add(key, value);
            }
            else {
                date.setTime(sdf.parse(str[2]));

                /*
                For creating the range from start to end date, we should transform them into key
                format, such as all keys in the tree.
                */

                int key1 = Integer.parseInt(String.valueOf(date.getTimeInMillis()/86400000));
                date.setTime(sdf.parse(str[4]));
                int key2 = Integer.parseInt(String.valueOf(date.getTimeInMillis()/86400000));
                ArrayList<Integer> report = tree.lookupRange(key1,key2);

                /*
                LookupRange method returns ArrayList, therefore we must calculate sum of the elements
                manually.
                */
                int sum = 0;
                if (report!=null) {
                    for (Integer j : report) {
                        sum += j;
                    }
                }
                /* Printing the result for lookupRange operation */
                System.out.println(sum);
            }
        }
    }
}

/* Interface for B-Tree */
interface RangeMap <K,V> {
    int size(); // returns the size of the Tree
    boolean isEmpty(); // returns true if Tree is empty
    void add(K key, V value); // insert new item into the map
    boolean contains(K key); // check if a key is present
    int lookup(K key); // lookup a value by the key
    ArrayList<Integer> lookupRange(K from, K to); // lookup values for a range of keys
}

/* Implementation of the B-Tree interface */
class BTree implements RangeMap<Integer, Integer>{

    /* Declaration of tree default parameters*/

    static int T=3;
    int size=0;
    Node root;
    BTree(){
        Node x = new Node();
        x.leaf=true;
        x.n=0;
        root=x;
    }

    /* Class that declares the element of the Tree (Key and Value pair) */
    static class Element {
        int key;
        int value;
        Element(int key, int value){
            this.key=key;
            this.value=value;
        }
    }

    /* Class that declares the node of the Tree */
    static class Node{
        int n;
        Element[] keys;
        boolean leaf;
        Node[] child;
        Node(){
            this.keys = new Element[2*T-1];
            this.child = new Node[2*T];
        }
    }

    /*
    This class needed to provide proper work of search method. In search method we should return Node and
    index of the searched element, so this class helps combine these things together
    */
    static class toRet{
        Node x;
        int i;
        toRet(Node x, int i){
            this.x = x;
            this.i = i;
        }
    }

    /* Worst case time complexity = O(1) */
    @Override
    public int size() {
        return this.size;
    }

    /* Worst case time complexity = O(1) */
    @Override
    public boolean isEmpty() {
        return this.size!=0;
    }

    /* Implementation of the add, addNonFull, split and search methods was carried out
       with the help of DSA Coursebook "Introduction to Algorithms" by T.Cormen*/

    @Override
    /* Worst case time complexity = O(t*log_t_(n)) */
    public void add(Integer key, Integer value) {
            Element a = new Element(key, value);
            Node r = root;
            size++;
            if (root.n == 2 * T - 1) {
                Node s = new Node();
                root = s;
                s.leaf = false;
                s.n = 0;
                s.child[0] = r;
                split(s, 0);
                addNonFull(s, a);
            } else addNonFull(r, a);
    }

    /* Worst case time complexity = O(t*log_t_(n)) */
    void addNonFull(Node x, Element a){
        int i = x.n-1;
        if (x.leaf){
            while (i>=0 && a.key<x.keys[i].key) {
                x.keys[i + 1] = x.keys[i];
                i--;
            }
            x.keys[i+1]=a;
            x.n=x.n+1;
        }
        else {
            while (i>=0 && a.key<x.keys[i].key){
                i--;
            }
            i++;
            if (x.child[i].n==2*T-1){
                split(x,i);
                if (a.key>x.keys[i].key)
                   i++;
            }
            addNonFull(x.child[i],a);
        }
    }

    /* Worst case time complexity = O(t) */
    void split(Node x, int i){
        Node z = new Node();
        Node y = x.child[i];
        z.leaf=y.leaf;
        z.n = T-1;
        for (int j = 0; j < T-1; j++) {
            z.keys[j]=y.keys[j+T];
        }
        if (y.leaf==false)
            for (int j = 0; j < T; j++)
                z.child[j]=y.child[j+T];
        y.n=T-1;
        for (int j = x.n; j >= i+1 ; j--) {
            x.child[j+1]=x.child[j];
        }
        x.child[i+1]=z;
        for (int j = x.n-1; j >= i ; j--) {
            x.keys[j+1]=x.keys[j];
        }
        x.keys[i]=y.keys[T-1];
        x.n=x.n+1;
    }

    /* Worst case time complexity = O(t*log_t_(n)) */
    toRet search(Node x, int k){
        int i=0;
        while (i<x.n && k>x.keys[i].key)
            i++;
        if (i<x.n && k==x.keys[i].key)
            return(new toRet(x,i));
        else if (x.leaf) return null;
        else return search(x.child[i],k);
    }

    @Override
    /* Worst case time complexity = O(t*log_t_(n)) */
    public boolean contains(Integer key) {
        return search(root,key)!=null;
    }

    @Override
    /* Worst case time complexity = O(t*log_t_(n)) */
    public int lookup(Integer key) {
        toRet r = search(root,key);
        if (r!=null) return r.x.keys[r.i].value;
        else return 0;
    }

    /* Worst case time complexity = O(t*log_t_(n)) */
    @Override
    public ArrayList<Integer> lookupRange(Integer from, Integer to){
        ArrayList<Integer> range =new ArrayList<>();
        return SearchRange(range, root, from, to);
    }
    /*
       Worst case time complexity = O(t*log_t_(n))
       I decide to use recursion for partial tree traversal in case of interval [from;to]
    */
    private ArrayList<Integer> SearchRange(ArrayList<Integer> range,Node x, Integer from, Integer to) {
        int i = 0;

        /* To avoid NPTE, I always check whether node and key null or not */
        if (x == null)
            return null;
        if (x.keys[i]==null)
            return null;

        if (from < x.keys[i].key){
            SearchRange(range, x.child[i], from, to );
        }
        for (i = 0; i < x.n; i++) {
            if (x.keys[i]==null)
                break;
            if(to < x.keys[i].key)
                break;
            if(!x.leaf && x.child.length >= i+1)
                SearchRange(range, x.child[i+1], from, to );
            if (from<= x.keys[i].key && to>= x.keys[i].key) {
                range.add(x.keys[i].value);
            }
        }
        return range;
    }

}