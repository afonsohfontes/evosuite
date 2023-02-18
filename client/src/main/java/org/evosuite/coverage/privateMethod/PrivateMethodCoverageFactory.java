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
import org.evosuite.coverage.MethodNameMatcher;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;



public class PrivateMethodCoverageFactory extends
        AbstractFitnessFactory<PrivateMethodCoverageTestFitness> {

    private static final Logger logger = LoggerFactory.getLogger(PrivateMethodCoverageFactory.class);
    private final MethodNameMatcher matcher = new MethodNameMatcher();

    /*
     * (non-Javadoc)
     *
     * @see
     * org.evosuite.coverage.TestCoverageFactory#getCoverageGoals()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PrivateMethodCoverageTestFitness> getCoverageGoals() {
        List<PrivateMethodCoverageTestFitness> goals = new ArrayList<>();

        long start = System.currentTimeMillis();

        String className = Properties.TARGET_CLASS;
        Class<?> clazz = Properties.getTargetClassAndDontInitialise();

        if (clazz != null) {
            goals.addAll(getCoverageGoals(clazz, className));
           // goals.addAll(getCoverageGoals(clazz, className));
           // goals.addAll(getCoverageGoals(clazz, className));
            Class<?>[] innerClasses = clazz.getDeclaredClasses();
            for (Class<?> innerClass : innerClasses) {
                String innerClassName = innerClass.getCanonicalName();
                goals.addAll(getCoverageGoals(innerClass, innerClassName));
            }
        }
        goalComputationTime = System.currentTimeMillis() - start;
        return goals;
    }

    private List<PrivateMethodCoverageTestFitness> getCoverageGoals(Class<?> clazz, String className) {
        List<PrivateMethodCoverageTestFitness> goals = new ArrayList<>();

        /* // constructor calls - not an interesting goal
        Constructor<?>[] allConstructors = clazz.getDeclaredConstructors();
        for (Constructor<?> c : allConstructors) {
            if (TestUsageChecker.canUse(c)) {
                String methodName = "<init>" + Type.getConstructorDescriptor(c);
                logger.info("Adding goal for constructor " + className + "." + methodName);
                goals.add(new PrivateMethodCoverageTestFitness(className, methodName));
            }
        }
        */
        /*
        Method[] allMethods = clazz.getDeclaredMethods();
        for (Method m : allMethods) {
            if (!TestUsageChecker.canUse(m)) {
                if (clazz.isEnum()) {
                    if (m.getName().equals("valueOf") || m.getName().equals("values")
                            || m.getName().equals("ordinal")) {
                        logger.debug("Excluding valueOf for Enum " + m);
                        continue;
                    }
                }
                if (clazz.isInterface() && Modifier.isAbstract(m.getModifiers())) {
                    // Don't count interface declarations as targets
                    continue;
                }
                String methodName = m.getName() + Type.getMethodDescriptor(m);
                if (!matcher.methodMatches(methodName)) {
                    logger.info("Method {} does not match criteria. ", methodName);
                    continue;
                }
                logger.info("Adding goal for method " + className + "." + methodName);
                goals.add(new PrivateMethodCoverageTestFitness(className, methodName));
            }
        }
         */

        // 1 GOAL PER PRIVATE METHOD
        //List<Object> privateMethods = new ArrayList<Object>();
        //List<Object> publicMethods = new ArrayList<Object>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())){
               // publicMethods.add(method.getName());
            }else {
                String s = method.getName();
                if (s != "__STATIC_RESET"){
                     logger.info("Adding goal for method " + className + "." + method.getName());
                     goals.add(new PrivateMethodCoverageTestFitness(className, method.getName()));
                     List<PrivateMethodCoverageTestFitness> g = goals;
                }
            }
        }

        return goals;
    }

    /**
     * Create a fitness function for branch coverage aimed at covering the root
     * branch of the given method in the given class. Covering a root branch
     * means entering the method.
     *
     * @param className a {@link String} object.
     * @param method    a {@link String} object.
     * @return a {@link org.evosuite.coverage.branch.BranchCoverageTestFitness}
     * object.
     */
    public static PrivateMethodCoverageTestFitness createMethodTestFitness(
            String className, String method) {

        return new PrivateMethodCoverageTestFitness(className,
                method.substring(method.lastIndexOf(".") + 1));
    }

    /**
     * Convenience method calling createMethodTestFitness(class,method) with
     * the respective class and method of the given BytecodeInstruction.
     *
     * @param instruction a {@link BytecodeInstruction} object.
     * @return a {@link org.evosuite.coverage.branch.BranchCoverageTestFitness}
     * object.
     */
    public static PrivateMethodCoverageTestFitness createMethodTestFitness(
            BytecodeInstruction instruction) {
        if (instruction == null)
            throw new IllegalArgumentException("null given");

        return createMethodTestFitness(instruction.getClassName(),
                instruction.getMethodName());
    }
}
