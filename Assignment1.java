
package assignment1;

import static assignment1.MyDlist.cloneList;
import java.io.IOException;

public class Assignment1 {

    public static void main(String[] args) throws IOException {

    MyDlist testListFile = new MyDlist("myfile.txt");
    MyDlist testListKeyboard = new MyDlist("stdin");
    
    
    System.out.println("From file :");
    testListFile.PrintList();
    
    System.out.println();
    System.out.println("From keyboard :");
    testListKeyboard.PrintList();

    MyDlist keyboardClone = MyDlist.cloneList(testListKeyboard);
    System.out.println();
    System.out.println("Cloned from keyboard :");
    keyboardClone.PrintList();
    
    MyDlist testUnion = MyDlist.union(testListFile, testListKeyboard);
    System.out.println();
    System.out.println("Test union :");
    testUnion.PrintList();

    MyDlist testIntersection = MyDlist.intersection(testListFile, testListKeyboard);
    System.out.println();
    System.out.println("Test intersection :");
    testIntersection.PrintList();
    
    }
    
        /** OLD UNION METHOD NOT USED */
    public static MyDlist unionSimple(MyDlist u, MyDlist v) {
        MyDlist unionList = cloneList(v);
        String element;
        Boolean found = false;
        DNode uNode = u.header;
        DNode vNode = v.header;
        
        while ((uNode = uNode.getNext()) != u.trailer) {
            element = uNode.getElement();
            while ((vNode = vNode.getNext()) != v.trailer) {
                if (vNode.getElement().equals(element)) {
                    found = true;
                    break;
                }
            }
            if (!found)
                unionList.addBefore(unionList.trailer, new DNode(element, null, null));
            vNode = v.header;
            found = false;
        }
        return unionList;
    }
    
    /** OLD INTERSECT METHOD NOT USED */
    public static MyDlist intersectionSimple(MyDlist u, MyDlist v) {
        MyDlist intersectList = new MyDlist();
        String element;
        DNode uNode = u.header;
        DNode vNode = v.header;
        while ((uNode = uNode.getNext()) != u.trailer) {
            element = uNode.getElement();
            while ((vNode = vNode.getNext()) != v.trailer) {
                if (vNode.getElement().equals(element))
                    intersectList.addBefore(intersectList.trailer, new DNode(element, null, null));
                // could add break here
            }
            vNode = v.header;
        }
        return intersectList;        
    }
}
