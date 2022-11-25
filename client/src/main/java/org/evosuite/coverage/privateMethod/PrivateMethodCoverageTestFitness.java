/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.coverage.privateMethod;

import org.evosuite.Properties;
import org.evosuite.ga.archive.Archive;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.EntityWithParametersStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
//


import java.util.*;

public class PrivateMethodCoverageTestFitness extends TestFitnessFunction {

    private static final long serialVersionUID = 1624503060256855484L;

    protected final String className;
    protected final String methodName;


    public String getMethod() {
        return methodName;
    }

    public PrivateMethodCoverageTestFitness(String className, String methodName) {
        this.className = Objects.requireNonNull(className, "className cannot be null");
        this.methodName = Objects.requireNonNull(methodName, "methodName cannot be null");
    }

    public String getClassName() {
        return className;
    }

    static float sumOfGP(float a, float r, int n)
    {
        float sum = 0;
        for (int i = 0; i < n; i++)
        {
            sum = sum + a;
            a = a * r;
        }
        return sum;
    }

/*
    @Override
    public double getFitness(TestChromosome individual, ExecutionResult result) {

        double fitness = 1.0;
        Class<?> c = Properties.getInitializedTargetClass();

        List<Object> privateMethods = new ArrayList<Object>();
        List<Object> publicMethods = new ArrayList<Object>();
        for (Method method : c.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                publicMethods.add(method.getName());
            } else {
                String s = method.getName().toString();
                if (s != "__STATIC_RESET") {
                    privateMethods.add(method.getName());
                }
            }
        }

        Set<String> coveredMethods = result.getTrace().getCoveredMethods();

        Map<String, Integer> coveredMethodsCount = result.getTrace().getMethodExecutionCount();
        float scoreMax = (float) (1.0 / privateMethods.size());
        double fitnessLocal = 0.0;
        for (Object pMethod : privateMethods) {
            for (String str : coveredMethods) {
                String a = pMethod.toString();
                if (str.contains(a)) {
                    //make sure it the same number of methods is called
                    int n = coveredMethodsCount.get(str);
                    float t = sumOfGP(scoreMax / 2, 0.5F, n);
                    fitnessLocal = fitnessLocal + t;

                }
            }
        }
        fitness = 1 - fitnessLocal;

        //maybe call / track goals here ex: add 1 goal per private method / ex2: add a nr of calls per method
        // see method coverage
        updateIndividual(individual, fitness);
        // UNDERSTAND GOALS / SET OF GOALS / AND HOW THEY ARE HANDLED
        // TRY TO GET/IMPLEMENT DEFECTS4J (build ar file and replace evosuite in defcts for J)
        //USE JAVA 8 TO MAKE SURE IT WORKS WITH DEFECTS4J


        if (fitness <= scoreMax/2) {
            individual.getTestCase().addCoveredGoal(this);
        }
        if (Properties.TEST_ARCHIVE) {
            Archive.getArchiveInstance().updateArchive(this, individual, fitness);
        }
        return fitness;
    }
*/


    @Override
    public double getFitness(TestChromosome individual, ExecutionResult result) {
        double fitness = 1.0;

        List<Integer> exceptionPositions = asSortedList(result.getPositionsWhereExceptionsWereThrown());
        for (String stmt : result.getTrace().getCoveredMethods()) {

                if (stmt.contains(className) && stmt.contains(methodName)) {
                    fitness = 0.0;
                    break;
                }
        }

        updateIndividual(individual, fitness);

        if (fitness == 0.0) {
            individual.getTestCase().addCoveredGoal(this);
        }

        if (Properties.TEST_ARCHIVE) {
            Archive.getArchiveInstance().updateArchive(this, individual, fitness);
        }






        return fitness;
    }



    private boolean isValidPosition(List<Integer> exceptionPositions, Integer position) {
        if (Properties.BREAK_ON_EXCEPTION) {
            return exceptionPositions.isEmpty() || position <= exceptionPositions.get(0);
        } else {
            return true;
        }
    }



    private <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<>(c);
        java.util.Collections.sort(list);
        return list;
    }


    @Override
    public String toString() {
        return "[METHOD] " + className + "." + methodName;
    }

    @Override
    public int hashCode() {
        int iConst = 13;
        return 51 * iConst + className.hashCode() * iConst + methodName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PrivateMethodCoverageTestFitness other = (PrivateMethodCoverageTestFitness) obj;
        if (!className.equals(other.className)) {
            return false;
        } else return methodName.equals(other.methodName);
    }

    @Override
    public int compareTo(TestFitnessFunction other) {
        if (other instanceof PrivateMethodCoverageTestFitness) {
            PrivateMethodCoverageTestFitness otherMethodFitness = (PrivateMethodCoverageTestFitness) other;
            if (className.equals(otherMethodFitness.getClassName()))
                return methodName.compareTo(otherMethodFitness.getMethod());
            else
                return className.compareTo(otherMethodFitness.getClassName());
        }
        return compareClassName(other);
    }


    @Override
    public String getTargetClass() {
        return getClassName();
    }

    @Override
    public String getTargetMethod() {
        return getMethod();
    }

}
