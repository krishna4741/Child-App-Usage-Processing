package com.optiquall.childappusage.util;

class Test extends Thread {
    public static void main(String args[]) {
        Test t1 = new Test();
        t1.start();
    }

    public void run() {
        System.out.println("thread is running...");
    }
}