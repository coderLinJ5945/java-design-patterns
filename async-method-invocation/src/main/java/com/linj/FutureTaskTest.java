package com.linj;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * FutureTask 异步任务调用模式测试类
 */
public class FutureTaskTest {

    public static void main(String[] args) {
        MyTask myTask = new MyTask("11", "22");//实例化任务，传递参数
        FutureTask<Object> futureTask = new FutureTask<>(myTask);//将任务放进FutureTask里
        //采用thread来开启多线程，futuretask继承了Runnable，可以放在线程池中来启动执行
        Thread thread = new Thread(futureTask);
        thread.start();

        try {
            //get():获取任务执行结果，如果任务还没完成则会阻塞等待直到任务执行完成。如果任务被取消则会抛出CancellationException异常，
            //如果任务执行过程发生异常则会抛出ExecutionException异常，如果阻塞等待过程中被中断则会抛出InterruptedException异常。
            boolean result = (boolean) futureTask.get();
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static class MyTask  implements Callable<Object> {
        private String args1;
        private String args2;
        //构造函数，用来向task中传递任务的参数
        public  MyTask(String args1,String args2) {
            this.args1=args1;
            this.args2=args2;
        }
        //任务执行的动作
        @Override
        public Object call() throws Exception {

            for(int i=0;i<100;i++){
                System.out.println(args1+args2+i);
            }
            return true;
        }
    }
}


