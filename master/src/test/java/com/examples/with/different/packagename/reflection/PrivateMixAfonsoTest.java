package com.examples.with.different.packagename.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;




public class PrivateMixAfonsoTest {

   /* public static <string> void main(String[] args) throws NoSuchFieldException, SecurityException, NoSuchMethodException {
        Field field = PrivateMixAfonsoTest.class.getDeclaredField("sampleField");
        Method[] a = PrivateMixAfonsoTest.class.getMethods();
    }
    public String sampleField;

    public final String getSampleField(){
        return sampleField;
    }

    public void setSampleField(String sampleField) {
        this.sampleField = sampleField;
    }
*/
    public void example(int nn) {
            float number = 1.0F;
            if (getN(nn)){
                number = 0.0F;
            }
        }

        private boolean getN(int n){
            if (n>20) {
                return checkIfOK();
            }    else{
                return false;
            }
        }

        private boolean checkIfOK(){
                 return true;
        }
}
