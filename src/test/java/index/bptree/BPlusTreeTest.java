package index.bptree;

import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class BPlusTreeTest {
    BPlusTree bpt = new BPlusTree(3);
    float num;
    @Before
    public void insert() {
        bpt.clear();
        Random r = new Random();
        for (int i = 0; i < 100; i++){
            num = r.nextFloat()*100;
            bpt.insert(num, num);
        }

    }

    @Test
    public void find() {

        assertEquals("not same", bpt.find(num), num);
        assertNull(bpt.find(101F));


    }

    @Test
    public void delete() {
//        BPlusTree bpt = new BPlusTree(3);
//        Random r = new Random();
//        float num = 0;
//        for (int i = 0; i < 100; i++){
//            num = r.nextFloat()*100;
//            bpt.insert(num, num);
//        }

        bpt.delete(100);
//        bpt.delete(num);
    }

    @Test
    public void update() {
        BPlusTree bpt = new BPlusTree(3);
        Random r = new Random();
        float num = 0;
        for (int i = 0; i < 100; i++){
            num = r.nextFloat()*100;
            bpt.insert(num, num);
        }

        bpt.update(100, 100);
        bpt.update(num, num);
    }

    @Test
    public void view() {
        BPlusTree bpt = new BPlusTree(3);
        Random r = new Random();
        for (int i = 0; i < 100; i++){
            float num = r.nextFloat()*100;
            bpt.insert(num, num);
        }
        bpt.view();

    }
}