package assignment3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;

public class TaskScheduler {
    
    /* ********* Time Complexity Discussion ********
    The tasks are added to a NodePositionList which is then sorted by release
    time using a mergesort algorithm.
    Mergesort splits the list into 2 sub-lists according to the middle element.
    The sub-lists are recursively mergesorted and then merged to give the result.
    The maximum number of elements n in a mergesort tree is 2^(h+1) - 1 where h
    is the height. Thus h is O(log n).
    At depth i of the mergesort tree there are 2^i sub-lists each of length 
    n / (2^i).  Since each element is visited once during the division into 
    sub-lists and then once to merge the sub-lists, the total work done at depth
    i is O(n), independent of i. Note that the comparator for integers is O(1).
    For the tree as a whole the running time is O(n) per depth and has O(log n)
    height making O(n log n).
    
    Each task is added to the HeapPriorityQueue when it is released and then 
    removed when scheduled later.  A HeapPriorityQueue is a complete binary tree so 
    adding an entry takes O(log n).  This is because an entry is added in the last 
    row and may need to bubble up the height of tree, which is log n.
    After removing the root of the complete binary tree the replacement entry may
    need to bubble down the height of the tree so also takes O(log n).
    Since n tasks are added and removed the process takes O(n log n).
    
    Both sort and priority queue stages take O(n log n) so the algorithm as a 
    whole is O(n log n).
    */
    static void scheduler(String file1, String file2, Integer m) {
        Scanner s;
        String name;
        Integer release;
        Integer deadline;        
        NodePositionList<Task> tasks = new NodePositionList();
        
        // Open input file or give an error if it does not exist.
        try {
            s = new Scanner(new File(file1));
        } catch (FileNotFoundException ex) {
            System.out.println(file1 + " does not exist.");
            return;
        }

        // Read the input file one token (word) at a time whilst verifying that
        // each task has a release and deadline that are both integers.
        while (s.hasNext()) {
            name = s.next();
            try {    
                release = Integer.parseInt(s.next());
                deadline = Integer.parseInt(s.next());
            } catch (NoSuchElementException ex) {
                System.out.println("Task attribute data does not follow required format.");
                return;            
            } catch (NumberFormatException ex) {
                System.out.println("Task attribute data does not follow required format.");
                return;            
            }                                
            if (release < 0 || deadline <= 0 || deadline <= release) {
                System.out.println("Task attribute data does not follow required format.");
                return;                            
            }
            // Create a new instance of Task and add to NodePositionList
            tasks.addLast(new Task(name, release, deadline));
        }            
        s.close();
                
        // Mergesort the NodePositionList of tasks by release time
        Comparator<Task> c = new DefaultComparator<>();
        Sort.mergeSort(tasks, c);
                
        // Create a HeapPriorityQueue with key of deadline and value of task. 
        HeapPriorityQueue<Integer, Task> heapPriorityQueue = new HeapPriorityQueue();
        Integer time = 0;
        Task task = null;
        String output = "";
        Entry<Integer, Task> queueEntry;
        
        Iterator<Task> taskIterator = tasks.iterator();
        if (taskIterator.hasNext())
            task = taskIterator.next();
                
        // For each time, put all ready tasks into the priority queue, take the m tasks 
        // with lowest deadlines out of the queue, and check if a deadline has been missed.
        do {
            // Insert any tasks released at current time into the priority queue
            while (task != null && Objects.equals(task.release, time)) {
                heapPriorityQueue.insert(task.deadline, task);
                task = null;
                if (!taskIterator.hasNext())
                    break;
                else
                    task = taskIterator.next();
            }            
            
            // Take a number of tasks out of the queue equal to the number of cores
            // (provided that sufficient tasks are released).
            output += "\n" + "Time : " + time + "\n";  /// *** FOR TESTING ONLY
            for (int i = 0; i < m; ++i) {
                if (!heapPriorityQueue.isEmpty()) {
                    queueEntry = heapPriorityQueue.removeMin();
                    output += queueEntry.getValue().name + " " + queueEntry.getValue().release
                            + " " + queueEntry.getValue().deadline // *** FOR TESTING ONLY
                            + "\n";
                }
            }
            
            ++time;
            // If the task at the front of the queue (i.e. it has not been started) has a deadline
            // of or before the new time, then its deadline has been missed.
            if (!heapPriorityQueue.isEmpty() && heapPriorityQueue.min().getKey() <= time) {
                System.out.println("Cannot be scheduled");
                return;
            }                          
        
        // Stop when there are no more tasks to be added to the queue (ie task == null)
        // and all tasks have been removed from the queue.
        } while (!(task == null && heapPriorityQueue.isEmpty()));
        
        // Create output file and give warning if it already exists.
        if (new File(file2).isFile())
            System.out.println(file2 + " already exists and will be overwritten.");
        // Print schedule to file.
        FileWriter writer = null;
        try {
            writer = new FileWriter(file2 , false);
        } catch (IOException ex) {
            System.out.println(ex);
        }
        PrintWriter printToFile = new PrintWriter(writer);
        printToFile.println(output);
        System.out.println(output);  // *** FOR TESTING ONLY
        printToFile.close();
    }
    
    // *** FOR TESTING ONLY
    public static void main(String[] args) {
            TaskScheduler.scheduler("jaketest1.txt", "feasibleschedule1.txt", 3);
    }
}

// Class to hold each task
class Task implements Comparable {
    String name;
    Integer release;
    Integer deadline;
    Task (String name, Integer release, Integer deadline) {
        this.name = name;
        this.release = release;
        this.deadline = deadline;
    }

    // Compare release times
    @Override
    public int compareTo(Object task) {
        if (this.release > ((Task) task).release)
            return 1;
        else
            return -1;
    }
}


// ******************** NET.DATASTRUCTURES **************************
// The following classes and interfaces are taken from package 
// net.datastrcutures version 4.0.

/**
 * Class containing various sorting algorithms.
 *
 * @author Michael Goodrich, Roberto Tamassia, Eric Zamore
 */
class Sort {
  //begin#fragment mergeSort
  /**
   * Sorts the elements of list in in nondecreasing order according
   * to comparator c, using the merge-sort algorithm.
   **/
  public static <E> void mergeSort (PositionList<E> in, Comparator<E> c) {
    int n = in.size();
    if (n < 2) 
      return;  // the in list is already sorted in this case
    // divide
    PositionList<E> in1 = new NodePositionList<E>(); 
    PositionList<E> in2 = new NodePositionList<E>(); 
    int i = 0;
    while (i < n/2) {
      in1.addLast(in.remove(in.first())); // move the first n/2 elements to in1
      i++;
    }
    while (!in.isEmpty())
      in2.addLast(in.remove(in.first())); // move the rest to in2
    // recur
    mergeSort(in1,c);
    mergeSort(in2,c);
    //conquer
    merge(in1,in2,c,in);
  }
  //end#fragment mergeSort
  
  //begin#fragment merge
  /**
   * Merges two sorted lists, in1 and in2, into a sorted list in.
   **/
  public static <E> void merge(PositionList<E> in1, PositionList<E> in2, 
         Comparator<E> c, PositionList<E> in) {
    while (!in1.isEmpty() && !in2.isEmpty())
      if (c.compare(in1.first().element(), in2.first().element()) <= 0)
        in.addLast(in1.remove(in1.first()));
      else
        in.addLast(in2.remove(in2.first()));
    while(!in1.isEmpty()) // move the remaining elements of in1
      in.addLast(in1.remove(in1.first()));
    while(!in2.isEmpty()) // move the remaining elements of in2
      in.addLast(in2.remove(in2.first()));
  }
  //end#fragment merge
}

class NodePositionList<E> implements PositionList<E> {
//end#fragment Header
//begin#fragment Listvars
  protected int numElts;            	// Number of elements in the list
  protected DNode<E> header, trailer;	// Special sentinels
//end#fragment Listvars
//begin#fragment checkPosition
  /** Constructor that creates an empty list; O(1) time */
  public NodePositionList() {
    numElts = 0;
    header = new DNode<E>(null, null, null);	// create header
    trailer = new DNode<E>(header, null, null);	// create trailer
    header.setNext(trailer);	// make header and trailer point to each other
  }
  /** Checks if position is valid for this list and converts it to
    *  DNode if it is valid; O(1) time */
  protected DNode<E> checkPosition(Position<E> p)
    throws InvalidPositionException {
    if (p == null)
      throw new InvalidPositionException
	("Null position passed to NodeList");
    if (p == header)
	throw new InvalidPositionException
	  ("The header node is not a valid position");
    if (p == trailer)
	throw new InvalidPositionException
	  ("The trailer node is not a valid position");
    try {
      DNode<E> temp = (DNode<E>) p;
      if ((temp.getPrev() == null) || (temp.getNext() == null))
	throw new InvalidPositionException
	  ("Position does not belong to a valid NodeList");
      return temp;
    } catch (ClassCastException e) {
      throw new InvalidPositionException
	("Position is of wrong type for this list");
    }
  }
  //end#fragment checkPosition
  //begin#fragment first
  /** Returns the number of elements in the list;  O(1) time */
  public int size() { return numElts; }
  /** Returns whether the list is empty;  O(1) time  */
  public boolean isEmpty() { return (numElts == 0); }
  /** Returns the first position in the list; O(1) time */
  public Position<E> first()
      throws EmptyListException {
    if (isEmpty())
      throw new EmptyListException("List is empty");
    return header.getNext();
  }
  //end#fragment first
  /** Returns the last position in the list; O(1) time */
  public Position<E> last()
      throws EmptyListException {
    if (isEmpty())
      throw new EmptyListException("List is empty");
    return trailer.getPrev();
  }
  //begin#fragment first
  /** Returns the position before the given one; O(1) time */
  public Position<E> prev(Position<E> p)
      throws InvalidPositionException, BoundaryViolationException {
    DNode<E> v = checkPosition(p);
    DNode<E> prev = v.getPrev();
    if (prev == header)
      throw new BoundaryViolationException
	("Cannot advance past the beginning of the list");
    return prev;
  }
  //end#fragment first
  /** Returns the position after the given one; O(1) time */
  public Position<E> next(Position<E> p)
      throws InvalidPositionException, BoundaryViolationException {
    DNode<E> v = checkPosition(p);
    DNode<E> next = v.getNext();
    if (next == trailer)
      throw new BoundaryViolationException
	("Cannot advance past the end of the list");
    return next;
  }
  //begin#fragment first
  /** Insert the given element before the given position;
    * O(1) time  */
  public void addBefore(Position<E> p, E element) 
      throws InvalidPositionException {
    DNode<E> v = checkPosition(p);
    numElts++;
    DNode<E> newNode = new DNode<E>(v.getPrev(), v, element);
    v.getPrev().setNext(newNode);
    v.setPrev(newNode);
  }
  //end#fragment first
  /** Insert the given element after the given position;
    * O(1) time  */
  public void addAfter(Position<E> p, E element) 
      throws InvalidPositionException {
    DNode<E> v = checkPosition(p);
    numElts++;
    DNode<E> newNode = new DNode<E>(v, v.getNext(), element);
    v.getNext().setPrev(newNode);
    v.setNext(newNode);
  }
  //begin#fragment remove
  /** Insert the given element at the beginning of the list, returning
    * the new position; O(1) time  */
  public void addFirst(E element) {
    numElts++;
    DNode<E> newNode = new DNode<E>(header, header.getNext(), element);
    header.getNext().setPrev(newNode);
    header.setNext(newNode);
  }
  //end#fragment remove
  /** Insert the given element at the end of the list, returning
    * the new position; O(1) time  */
  public void addLast(E element) {
    numElts++;
    DNode<E> oldLast = trailer.getPrev();
    DNode<E> newNode = new DNode<E>(oldLast, trailer, element);
    oldLast.setNext(newNode);
    trailer.setPrev(newNode);
  }
  //begin#fragment remove
  /**Remove the given position from the list; O(1) time */
  public E remove(Position<E> p)
      throws InvalidPositionException {
    DNode<E> v = checkPosition(p);
    numElts--;
    DNode<E> vPrev = v.getPrev();
    DNode<E> vNext = v.getNext();
    vPrev.setNext(vNext);
    vNext.setPrev(vPrev);
    E vElem = v.element();
    // unlink the position from the list and make it invalid
    v.setNext(null);
    v.setPrev(null);
    return vElem;
  }
  /** Replace the element at the given position with the new element
    * and return the old element; O(1) time  */
  public E set(Position<E> p, E element)
      throws InvalidPositionException {
    DNode<E> v = checkPosition(p);
    E oldElt = v.element();
    v.setElement(element);
    return oldElt;
  }
  //end#fragment remove

//begin#fragment Iterator
  /** Returns an iterator of all the elements in the list. */
  public Iterator<E> iterator() { return new ElementIterator<E>(this); }
//end#fragment Iterator
//begin#fragment PIterator
  /** Returns an iterable collection of all the nodes in the list. */
  public Iterable<Position<E>> positions() {     // create a list of posiitons
    PositionList<Position<E>> P = new NodePositionList<Position<E>>();
    if (!isEmpty()) {
      Position<E> p = first();
      while (true) {
	P.addLast(p); // add position p as the last element of list P
	if (p == last())
	  break;
	p = next(p);
      }
    }
    return P; // return P as our Iterable object
  }
//end#fragment PIterator

  // Convenience methods
  /** Returns whether a position is the first one;  O(1) time */
  public boolean isFirst(Position<E> p)
    throws InvalidPositionException {  
    DNode<E> v = checkPosition(p);
    return v.getPrev() == header;
  }
  /** Returns whether a position is the last one;  O(1) time */
  public boolean isLast(Position<E> p)
      throws InvalidPositionException {  
    DNode<E> v = checkPosition(p);
    return v.getNext() == trailer;
  }
  /** Swap the elements of two give positions;  O(1) time */ 
  public void swapElements(Position<E> a, Position<E> b) 
      throws InvalidPositionException {
    DNode<E> pA = checkPosition(a);
    DNode<E> pB = checkPosition(b);
    E temp = pA.element();
    pA.setElement(pB.element());
    pB.setElement(temp);
  }
  /** Returns a textual representation of a given node list using for-each */
  public static <E> String forEachToString(PositionList<E> L) {
    String s = "[";
    int i = L.size();
    for (E elem: L) {
      s += elem; // implicit cast of the element to String
      i--;
      if (i > 0)
	s += ", "; // separate elements with a comma
    }
    s += "]";
    return s;
  }
//begin#fragment toString
  /** Returns a textual representation of a given node list */
  public static <E> String toString(PositionList<E> l) {
    Iterator<E> it = l.iterator();
    String s = "[";
    while (it.hasNext()) {
      s += it.next();	// implicit cast of the next element to String
      if (it.hasNext())
	s += ", ";
      }
    s += "]";
    return s;
  }
//end#fragment toString
  /** Returns a textual representation of the list */
  public String toString() {
    return toString(this);
  }
}

interface PositionList<E> extends Iterable<E> {
//end#fragment Header
//begin#fragment List
  /** Returns the number of elements in this list. */
  public int size();
  /** Returns whether the list is empty. */
  public boolean isEmpty();
  /** Returns the first node in the list. */
  public Position<E> first();
  /** Returns the last node in the list. */
  public Position<E> last();
  /** Returns the node after a given node in the list. */
  public Position<E> next(Position<E> p) 
    throws InvalidPositionException, BoundaryViolationException;
  /** Returns the node before a given node in the list. */
  public Position<E> prev(Position<E> p) 
    throws InvalidPositionException, BoundaryViolationException;
  /** Inserts an element at the front of the list, returning new position. */
  public void addFirst(E e);
  /** Inserts and element at the back of the list, returning new position. */
  public void addLast(E e);
  /** Inserts an element after the given node in the list. */
  public void addAfter(Position<E> p, E e) 
    throws InvalidPositionException;
  /** Inserts an element before the given node in the list. */
  public void addBefore(Position<E> p, E e) 
    throws InvalidPositionException;
  /** Removes a node from the list, returning the element stored there. */
  public E remove(Position<E> p) throws InvalidPositionException;
  /** Replaces the element stored at the given node, returning old element. */
  public E set(Position<E> p, E e) throws InvalidPositionException;
//end#fragment List
//begin#fragment Positions
  /** Returns an iterable collection of all the nodes in the list. */
  public Iterable<Position<E>> positions();
//end#fragment Positions
//begin#fragment Iterator
  /** Returns an iterator of all the elements in the list. */
  public Iterator<E> iterator();
//end#fragment Iterator
//begin#fragment Tail
}
//end#fragment Tail


class InvalidPositionException extends RuntimeException {  
  public InvalidPositionException(String err) {
    super(err);
  }
//end#fragment InvalidPositionException
  public InvalidPositionException() {
    /* default constructor */
  }
//begin#fragment InvalidPositionException
}
//end#fragment InvalidPositionException


interface Position<E> {
  /** Return the element stored at this position. */
  E element();
}
//end#fragment All

class BoundaryViolationException  extends RuntimeException {
  public BoundaryViolationException (String message) {
    super (message);
  }
}

class DNode<E> implements Position<E> {
  private DNode<E> prev, next;	// References to the nodes before and after
  private E element;	// Element stored in this position
  /** Constructor */
  public DNode(DNode<E> newPrev, DNode<E> newNext, E elem) {
    prev = newPrev;
    next = newNext;
    element = elem;
  }
  // Method from interface Position
  public E element() throws InvalidPositionException {
    if ((prev == null) && (next == null))
      throw new InvalidPositionException("Position is not in a list!");
    return element;
  }
  // Accessor methods
  public DNode<E> getNext() { return next; }
  public DNode<E> getPrev() { return prev; }
  // Update methods
  public void setNext(DNode<E> newNext) { next = newNext; }
  public void setPrev(DNode<E> newPrev) { prev = newPrev; }
  public void setElement(E newElement) { element = newElement; }
}
//end#fragment DNode

class ElementIterator<E> implements Iterator<E> {
  protected PositionList<E> list; // the underlying list
  protected Position<E> cursor; // the next position
  /** Creates an element iterator over the given list. */
  public ElementIterator(PositionList<E> L) {
    list = L;
    cursor = (list.isEmpty())? null : list.first();
  }
//end#fragment Iterator
  /** Returns whether the iterator has a next object. */
//begin#fragment Iterator
  public boolean hasNext() { return (cursor != null);  }
//end#fragment Iterator
  /** Returns the next object in the iterator. */
//begin#fragment Iterator
  public E next() throws NoSuchElementException {
    if (cursor == null)
      throw new NoSuchElementException("No next element");
    E toReturn = cursor.element();
    cursor = (cursor == list.last())? null : list.next(cursor);
    return toReturn;
  }
//end#fragment Iterator
  /** Throws an {@link UnsupportedOperationException} in all cases,
   * because removal is not a supported operation in this iterator.
   */
  public void remove() throws UnsupportedOperationException {
    throw new UnsupportedOperationException("remove");
  }
//begin#fragment Iterator
}
//end#fragment Iterator


class EmptyListException  extends RuntimeException {
  public EmptyListException (String message) {
    super (message);
  }
}

class DefaultComparator<E> implements Comparator<E> {
  /** Compares two given elements
 //end#fragment DefaultComparator
    *
    * @return a negative integer if <tt>a</tt> is less than <tt>b</tt>,
    * zero if <tt>a</tt> equals <tt>b</tt>, or a positive integer if
    * <tt>a</tt> is greater than <tt>b</tt>
//begin#fragment DefaultComparator
    */
  public int compare(E a, E b) throws ClassCastException { 
    return ((Comparable<E>) a).compareTo(b);
  }
}
//begin#fragment DefaultComparator


//begin#fragment HeapPriorityQueue
/** 
  * Realization of a priority queue by means of a heap.  A complete
  * binary tree realized by means of an array list is used to
  * represent the heap.
//end#fragment HeapPriorityQueue
  *
  * @author Roberto Tamassia, Michael Goodrich, Eric Zamore
//begin#fragment HeapPriorityQueue
  */
class HeapPriorityQueue<K,V> implements PriorityQueue<K,V> {
  protected CompleteBinaryTree<Entry<K,V>> heap;	// underlying heap
  protected Comparator<K> comp;	// comparator for the keys
  /** Inner class for heap entries. */
  protected static class  MyEntry<K,V> implements Entry<K,V> {
    protected K key;
    protected V value;
    public MyEntry(K k, V v) { key = k; value = v; }
    public K getKey() { return key; }
    public V getValue() { return value; }
    public String toString() { return "(" + key  + "," + value + ")"; }
  }
  /** Creates an empty heap with the default comparator */ 
  public HeapPriorityQueue() {  
    heap = new ArrayListCompleteBinaryTree<Entry<K,V>>(); // use an array list
    comp = new DefaultComparator<K>();     // use the default comparator
  }
  /** Creates an empty heap with the given comparator */
  public HeapPriorityQueue(Comparator<K> c) {
    heap = new ArrayListCompleteBinaryTree<Entry<K,V>>();
    comp = c;
  }
//end#fragment HeapPriorityQueue
  /** Sets the comparator used for comparing items in the heap. 
   * @throws IllegalStateException if priority queue is not empty */
  public void setComparator(Comparator<K> c) throws IllegalStateException {
    if(!isEmpty())  // this is only allowed if the priority queue is empty
      throw new IllegalStateException("Priority queue is not empty");
    comp = c;
  }
//begin#fragment HeapPriorityQueue
  /** Returns the size of the heap */
  public int size() { return heap.size(); } 
  /** Returns whether the heap is empty */
  public boolean isEmpty() { return heap.size() == 0; }
  //end#fragment HeapPriorityQueue
  //begin#fragment mainMethods
  /** Returns but does not remove an entry with minimum key */
  public Entry<K,V> min() throws EmptyPriorityQueueException {
    if (isEmpty()) 
      throw new EmptyPriorityQueueException("Priority queue is empty");
    return heap.root().element();
  }
  /** Inserts a key-value pair and returns the entry created */
  public Entry<K,V> insert(K k, V x) throws InvalidKeyException {
    checkKey(k);  // may throw an InvalidKeyException
    Entry<K,V> entry = new MyEntry<K,V>(k,x);
    upHeap(heap.add(entry));
    return entry;
  }
  /** Removes and returns an entry with minimum key */
  public Entry<K,V> removeMin() throws EmptyPriorityQueueException {
    if (isEmpty()) 
      throw new EmptyPriorityQueueException("Priority queue is empty");
    Entry<K,V> min = heap.root().element();
    if (size() == 1)
      heap.remove();
    else {
      heap.replace(heap.root(), heap.remove());
      downHeap(heap.root());
    }
    return min;
  }
  /** Determines whether a given key is valid */
  protected void checkKey(K key) throws InvalidKeyException {
    try {
      comp.compare(key,key);
    }
    catch(Exception e) {
      throw new InvalidKeyException("Invalid key");
    }
  }
  //end#fragment mainMethods
  //begin#fragment auxiliary
   /** Performs up-heap bubbling */
  protected void upHeap(Position<Entry<K,V>> v) {
    Position<Entry<K,V>> u;
    while (!heap.isRoot(v)) {
      u = heap.parent(v);
      if (comp.compare(u.element().getKey(), v.element().getKey()) <= 0) break;
      swap(u, v);
      v = u;
    }
  }
  /** Performs down-heap bubbling */
  protected void downHeap(Position<Entry<K,V>> r) {
    while (heap.isInternal(r)) {
      Position<Entry<K,V>> s;		// the position of the smaller child
      if (!heap.hasRight(r))
	s = heap.left(r);
      else if (comp.compare(heap.left(r).element().getKey(), 
                            heap.right(r).element().getKey()) <=0)
	s = heap.left(r);
      else
	s = heap.right(r);
      if (comp.compare(s.element().getKey(), r.element().getKey()) < 0) {
	swap(r, s);
	r = s;
      }
      else 
	break;
    }
  }
  /** Swaps the entries of the two given positions */
  protected void swap(Position<Entry<K,V>> x, Position<Entry<K,V>> y) {
    Entry<K,V> temp = x.element();
    heap.replace(x, y.element());
    heap.replace(y, temp);
  }
  /** Text visualization for debugging purposes */
  public String toString() {
    return heap.toString();
  }
  //end#fragment auxiliary
}

//begin#fragment Entry
/** Interface for a key-value pair entry **/
interface Entry<K,V> {
  /** Returns the key stored in this entry. */
  public K getKey();
  /** Returns the value stored in this entry. */
  public V getValue();
}
//end#fragment Entry

class InvalidKeyException  extends RuntimeException {
  public InvalidKeyException (String message) {
    super (message);
  }
  public static final long serialVersionUID = 424242L;
}

class EmptyPriorityQueueException  extends RuntimeException {
  public EmptyPriorityQueueException (String message) {
    super (message);
  }
}

//begin#fragment VectorHeap
class ArrayListCompleteBinaryTree<E> 
    implements CompleteBinaryTree<E>  {
  protected ArrayList<BTPos<E>> T;  // indexed list of tree positions
  /** Nested class for a index list-based complete binary tree node. */
  protected static class BTPos<E> implements Position<E> {
    E element; // element stored at this position
    int index;      // index of this position in the array list
    public BTPos(E elt, int i) { 
      element = elt;
      index = i; 
    }
    public E element() { return element; }
    public int index() { return index; }
    public E setElement(E elt) {
      E temp = element;
      element = elt;
      return temp;
    }
//end#fragment VectorHeap
    public String toString() {
      return("[" + element + "," + index + "]");
    }
//begin#fragment VectorHeap
  }
  /** default constructor */
  public ArrayListCompleteBinaryTree() { 
    T = new ArrayList<BTPos<E>>();
    T.add(0, null); // the location at rank 0 is deliberately empty
  }
  /** Returns the number of (internal and external) nodes. */
  public int size() { return T.size() - 1; } 
  /** Returns whether the tree is empty. */ 
  public boolean isEmpty() { return (size() == 0); } 
//end#fragment VectorHeap
//begin#fragment VectorHeap2
  /** Returns whether v is an internal node. */
  public boolean isInternal(Position<E> v) throws InvalidPositionException {
    return hasLeft(v);  // if v has a right child it will have a left child
  }
  /** Returns whether v is an external node. */
  public boolean isExternal(Position<E> v) throws InvalidPositionException {
    return !isInternal(v);
  }
  /** Returns whether v is the root node. */
  public boolean isRoot(Position<E> v) throws InvalidPositionException { 
    BTPos<E> vv = checkPosition(v);
    return vv.index() == 1;
  }
  /** Returns whether v has a left child. */
  public boolean hasLeft(Position<E> v) throws InvalidPositionException { 
    BTPos<E> vv = checkPosition(v);
    return 2*vv.index() <= size();
  }
  /** Returns whether v has a right child. */
  public boolean hasRight(Position<E> v) throws InvalidPositionException { 
    BTPos<E> vv = checkPosition(v);
    return 2*vv.index() + 1 <= size();
  }
  /** Returns the root of the tree. */
  public Position<E> root() throws EmptyTreeException {
    if (isEmpty()) throw new EmptyTreeException("Tree is empty");
    return T.get(1);
  } 
  /** Returns the left child of v. */
  public Position<E> left(Position<E> v) 
    throws InvalidPositionException, BoundaryViolationException { 
    if (!hasLeft(v)) throw new BoundaryViolationException("No left child");
    BTPos<E> vv = checkPosition(v);
    return T.get(2*vv.index());
  }
  /** Returns the right child of v. */ 
  public Position<E> right(Position<E> v) 
    throws InvalidPositionException { 
    if (!hasRight(v)) throw new BoundaryViolationException("No right child");
    BTPos<E> vv = checkPosition(v);
    return T.get(2*vv.index() + 1);
  }
//end#fragment VectorHeap2
//begin#fragment VectorHeap3
  /** Returns the parent of v. */
  public Position<E> parent(Position<E> v) 
    throws InvalidPositionException, BoundaryViolationException { 
    if (isRoot(v)) throw new BoundaryViolationException("No parent");
    BTPos<E> vv = checkPosition(v);
    return T.get(vv.index()/2);
  }
//end#fragment VectorHeap3
  /** Returns an iterable collection of the children of v. */ 
  public Iterable<Position<E>> children(Position<E> v) throws InvalidPositionException { 
    PositionList<Position<E>> children = new NodePositionList<Position<E>>();
    if (hasLeft(v))
      children.addLast(left(v));
    if (hasRight(v))
      children.addLast(right(v));
    return children;
  }
  /** Returns an iterable collection of all the nodes in the tree. */
  public Iterable<Position<E>> positions() {
    ArrayList<Position<E>> P = new ArrayList<Position<E>>();
    Iterator<BTPos<E>> iter = T.iterator();
    iter.next(); // skip the first position
    while (iter.hasNext())
      P.add(iter.next());
    return P;
  }
//begin#fragment VectorHeap3
  /** Replaces the element at v. */
  public E replace(Position<E> v, E o) throws InvalidPositionException {
    BTPos<E> vv = checkPosition(v);
    return vv.setElement(o);
  }
  /** Adds an element just after the last node (in a level numbering). */
  public Position<E> add(E e) {
    int i = size() + 1;
    BTPos<E> p = new BTPos<E>(e,i);
    T.add(i, p);
    return p;
  }
  /** Removes and returns the element at the last node. */
  public E remove() throws EmptyTreeException {
    if(isEmpty()) throw new EmptyTreeException("Tree is empty");
    return T.remove(size()).element(); 
  }
  /** Determines whether the given position is a valid node. */
  protected BTPos<E> checkPosition(Position<E> v) 
    throws InvalidPositionException 
  {
    if (v == null || !(v instanceof BTPos))
      throw new InvalidPositionException("Position is invalid");
    return (BTPos<E>) v;
  }
//end#fragment VectorHeap3
  // Additional Methods
  /** Returns the sibling of v. */ 
  public Position<E> sibling(Position<E> v) 
    throws InvalidPositionException, BoundaryViolationException {
    try {
      Position<E> p = parent(v);
      Position<E> lc = left(p);
      if (v == lc)
	return right(p);
      else
	return lc;
    }
    catch(BoundaryViolationException e) {
      throw new BoundaryViolationException("Node has no sibling");
    }
  }
  /** Swaps the elements at two nodes. */
  public void swapElements(Position<E> v, Position<E> w)
    throws InvalidPositionException {
    BTPos<E> vv = checkPosition(v);
    BTPos<E> ww = checkPosition(w);
    E temp = vv.element();
    vv.setElement(ww.element());
    ww.setElement(temp);
  }
//begin#fragment VectorHeap3
  /** Returns an iterator of the elements stored at all nodes in the tree. */
  public Iterator<E> iterator() { 
    ArrayList<E> list = new ArrayList<E>();
    Iterator<BTPos<E>> iter = T.iterator();
    iter.next(); // skip the first element
    while (iter.hasNext())
      list.add(iter.next().element());
    return list.iterator();
  } 
//end#fragment VectorHeap3
  /** Returns a String representing this complete binary tree. */
  public String toString() { return T.toString(); }
//begin#fragment VectorHeap3
} 
//end#fragment VectorHeap3

//begin#fragment HeapTree
interface CompleteBinaryTree<E> extends BinaryTree<E> {
//end#fragment HeapTree
  /** Adds an element to the tree just after the last node. Returns
   * the newly created position. */
//begin#fragment HeapTree
  public Position<E> add(E elem);
//end#fragment HeapTree
  /** Removes and returns the element stored in the last node of the
   * tree. */
//begin#fragment HeapTree
  public E remove();
}
//end#fragment HeapTree

interface BinaryTree<E> extends Tree<E> {
  /** Returns the left child of a node. */
  public Position<E> left(Position<E> v) 
    throws InvalidPositionException, BoundaryViolationException;
  /** Returns the right child of a node. */
  public Position<E> right(Position<E> v) 
    throws InvalidPositionException, BoundaryViolationException;
  /** Returns whether a node has a left child. */
  public boolean hasLeft(Position<E> v) throws InvalidPositionException;
  /** Returns whether a node has a right child. */
  public boolean hasRight(Position<E> v) throws InvalidPositionException;
}

interface Tree<E> {
  /** Returns the number of nodes in the tree. */
  public int size();
  /** Returns whether the tree is empty. */
  public boolean isEmpty();
  /** Returns an iterator of the elements stored in the tree. */
  public Iterator<E> iterator();
  /** Returns an iterable collection of the the nodes. */
  public Iterable<Position<E>> positions();
  /** Replaces the element stored at a given node. */
  public E replace(Position<E> v, E e)
    throws InvalidPositionException;
  /** Returns the root of the tree. */
  public Position<E> root() throws EmptyTreeException;
  /** Returns the parent of a given node. */
  public Position<E> parent(Position<E> v)
    throws InvalidPositionException, BoundaryViolationException;
  /** Returns an iterable collection of the children of a given node. */
  public Iterable<Position<E>> children(Position<E> v) 
    throws InvalidPositionException;
  /** Returns whether a given node is internal. */
  public boolean isInternal(Position<E> v) 
    throws InvalidPositionException;
  /** Returns whether a given node is external. */
  public boolean isExternal(Position<E> v) 
    throws InvalidPositionException;
  /** Returns whether a given node is the root of the tree. */
  public boolean isRoot(Position<E> v)
    throws InvalidPositionException;
}

class EmptyTreeException extends RuntimeException {  
  public EmptyTreeException(String err) {
    super(err);
  }
}

//begin#fragment PriorityQueue
/** Interface for the priority queue ADT */
interface PriorityQueue<K,V> {
  /** Returns the number of items in the priority queue. */
  public int size();
  /** Returns whether the priority queue is empty. */
  public boolean isEmpty();
  /** Returns but does not remove an entry with minimum key. */
  public Entry<K,V> min() throws EmptyPriorityQueueException;
  /** Inserts a key-value pair and return the entry created. */
  public Entry<K,V> insert(K key, V value) throws InvalidKeyException;
  /** Removes and returns an entry with minimum key. */
  public Entry<K,V> removeMin() throws EmptyPriorityQueueException;
}
//end#fragment PriorityQueue