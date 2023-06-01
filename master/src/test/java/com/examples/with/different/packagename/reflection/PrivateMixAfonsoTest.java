package com.examples.with.different.packagename.reflection;


public class PrivateMixAfonsoTest {

    public void example(int nn) throws Exception {
        float number = 1.0F;
        if (getRoot(nn)){
            number = 0.0F;
        }
    }

    private boolean getRoot(int n) throws Exception {
        if (n>20 && n<1001) {
            return check21_1000(n);
        }    else{
            if (n>1000){
                    return checkGreater1000(n);
                }
            }
            return checkIfBellow20();
        }

    private boolean check21_1000(int n) throws Exception {
        if (n%2==1){
            throw new Exception("Exception message");
        }
        return true;
    }
    private boolean checkIfBellow20(){
        return false;
    }
    private boolean checkGreater1000(int n) throws Exception {
        if (n%2==0){
            throw new Exception("Exception message");
        }
        return true;
    }
}