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
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import org.evosuite.coverage.privateMethod.fileWriter;


import static java.util.stream.Collectors.averagingDouble;

/**
 * Fitness function for a whole test suite for all methods, including exceptional behaviour
 *
 * @author Gordon Fraser, Jose Miguel Rojas
 */
public class PrivateMethodCoverageSuiteFitness extends TestSuiteFitnessFunction {

    //private static final long serialVersionUID = 1359321076367091582L;

    private final static Logger logger = LoggerFactory.getLogger(PrivateMethodCoverageSuiteFitness.class);

    // Each test gets a set of distinct covered goals, these are mapped by branch id
    protected final Map<String, TestFitnessFunction> methodCoverageMap = new LinkedHashMap<>();
    protected final int totalMethods;

    private final Set<String> toRemoveMethods = new LinkedHashSet<>();

    //coveredMethodsCount =
    private final Map<String, Integer>  suiteFitnessHelper = new HashMap();
    private final Set<String> removedMethods = new LinkedHashSet<>();

    // Some stuff for debug output
    protected int maxCoveredMethods = 0;
    protected double bestFitness = Double.MAX_VALUE;

    /**
     * <p>
     * Constructor for MethodCoverageSuiteFitness.
     * </p>
     */
    public PrivateMethodCoverageSuiteFitness() {
        determineCoverageGoals();
        totalMethods = methodCoverageMap.size();
        logger.info("Total methods: " + totalMethods);

    }

    /**
     * Initialize the set of known coverage goals
     */
    protected void determineCoverageGoals() {
        List<PrivateMethodCoverageTestFitness> goals = new PrivateMethodCoverageFactory().getCoverageGoals();
        for (PrivateMethodCoverageTestFitness goal : goals) {
            methodCoverageMap.put(goal.getClassName() + "." + goal.getMethod(), goal);
            if (Properties.TEST_ARCHIVE)
                Archive.getArchiveInstance().addTarget(goal);
        }
        //Archive a = Archive.getArchiveInstance();
       // int b = 0;
    }



    public double getCoverage() {
        final double cov = 1.0;
        assert (cov >= 0.0 && cov <= 1.0) : "Incorrect coverage value " + cov + ". Expected value between 0 and 1";
        return cov;
    }
    
    
    /**
     * If there is an exception in a super-constructor, then the corresponding
     * constructor might not be included in the execution trace
     *
     * @param test
     * @param result
     * @param calledMethods
     */
    protected void handleConstructorExceptions(TestChromosome test, ExecutionResult result, Set<String> calledMethods) {

        if (result.hasTimeout() || result.hasTestException()
                || result.noThrownExceptions())
            return;

        Integer exceptionPosition = result.getFirstPositionOfThrownException();
        Statement statement = result.test.getStatement(exceptionPosition);
        if (statement instanceof ConstructorStatement) {
            ConstructorStatement c = (ConstructorStatement) statement;
            String className = c.getConstructor().getName();
            String methodName = "<init>"
                    + Type.getConstructorDescriptor(c.getConstructor().getConstructor());
            String name = className + "." + methodName;
            if (methodCoverageMap.containsKey(name) && !calledMethods.contains(name)) {
                TestFitnessFunction goal = methodCoverageMap.get(name);

                // only include methods being called
                test.getTestCase().addCoveredGoal(goal);
                calledMethods.add(name);
                this.toRemoveMethods.add(name);

                if (Properties.TEST_ARCHIVE) {
                    Archive.getArchiveInstance().updateArchive(goal, test, 0.0);
                }
            }
        }

    }

    /**
     * Iterate over all execution results and summarize statistics
     *
     * @param results
     * @param calledMethods
     * @return
     */


    protected boolean analyzeTraces(List<ExecutionResult> results, Set<String> calledMethods) {
        boolean hasTimeoutOrTestException = false;

        for (ExecutionResult result : results) {
            if (result.hasTimeout() || result.hasTestException()) {
                hasTimeoutOrTestException = true;
                continue;
            }

            TestChromosome test = new TestChromosome();
            test.setTestCase(result.test);
            test.setLastExecutionResult(result);
            test.setChanged(false);

            for (String methodName : this.methodCoverageMap.keySet()) {
                TestFitnessFunction goal = this.methodCoverageMap.get(methodName);

                double fit = goal.getFitness(test, result); // archive is updated by the TestFitnessFunction class

                if (fit == 0.0) {
                    calledMethods.add(methodName); // helper to count the number of covered goals
                   //this.toRemoveMethods.add(methodName); // goal to not be considered by the next iteration of the evolutionary algorithm
                }
            }

            // In case there were exceptions in a constructor
            handleConstructorExceptions(test, result, calledMethods);
        }
        return hasTimeoutOrTestException;
    }


    static float sumOfGP(float a, float r, double n)
    {
        float sum = 0;
        for (int i = 0; i < n; i++)
        {
            sum = sum + a;
            a = a * r;
        }
        return sum;
    }

    protected void setCoverageGoalsSuiteLevel(List<ExecutionResult> results, ExecutionResult resultStop, String goalMethod){
        for (ExecutionResult result : results) {

            TestChromosome test = new TestChromosome();
            test.setTestCase(result.test);
            test.setLastExecutionResult(result);
            test.setChanged(false);

            TestFitnessFunction goal = methodCoverageMap.get(goalMethod);
            test.getTestCase().addCoveredGoal(goal);
/*
            if (Properties.TEST_ARCHIVE) {
                Archive.getArchiveInstance().updateArchive(goal, test, 0.0);
            }
*/
            if (resultStop==result){
                return;
            }
        }
    }


    protected boolean analyzeTraces_TestSuiteLevel(List<ExecutionResult> results, Set<String> calledMethods) {
        boolean hasTimeoutOrTestException = false;

        Map<String, Double> callsCounter = new HashMap<String, Double>();
        float scoreMax = (float) 1.0/this.methodCoverageMap.size();

        for (ExecutionResult result : results) {
            if (result.hasTimeout() || result.hasTestException()) {
                hasTimeoutOrTestException = true;
                continue;
            }

            TestChromosome test = new TestChromosome();
            test.setTestCase(result.test);
            test.setLastExecutionResult(result);
            test.setChanged(false);

            Set<String> coveredMethods = result.getTrace().getCoveredMethods();
            Map<String, Integer> coveredMethodsCount = result.getTrace().getMethodExecutionCount();




            for (String goalMethod : this.methodCoverageMap.keySet()) {
                if(callsCounter.containsKey(goalMethod)){
                }else{
                    callsCounter.put(goalMethod, 0.0);
                }
                for (String str : coveredMethods) {
                    if ((str.contains(goalMethod)) && !(calledMethods.contains(goalMethod))) {
                        //make sure it the same number of methods is called
                       int n = coveredMethodsCount.get(str);
                        callsCounter.put(goalMethod, callsCounter.get(goalMethod)+n);
                        double fitness = sumOfGP(scoreMax / 2, 0.5F, callsCounter.get(goalMethod));
                        if (fitness>= 0.98*scoreMax){ //0.98 or higher means the method was called ? times
                            calledMethods.add(goalMethod);
                            //this.toRemoveMethods.add(goalMethod);
                            TestFitnessFunction goal = methodCoverageMap.get(goalMethod);
                            test.getTestCase().addCoveredGoal(goal);
                            //setCoverageGoalsSuiteLevel(results, result, goalMethod);
                        }
                    }
                }
            }
            // In case there were exceptions in a constructor
            handleConstructorExceptions(test, result, calledMethods);
        }
        return hasTimeoutOrTestException;
    }



    /**
     * {@inheritDoc}
     * <p>
     * Execute all tests and count covered branches
     */
    @Override
    public double getFitness(TestSuiteChromosome suite) {

        logger.trace("Calculating PRIVATE method fitness");
        double fitness = 0.0;

        List<ExecutionResult> results = runTestSuite(suite);
        // Collect stats in the traces
        Set<String> calledMethods = new LinkedHashSet<>();
        boolean hasTimeoutOrTestException = analyzeTraces(results, calledMethods);
        // boolean hasTimeoutOrTestException = analyzeTraces_TestSuiteLevel(results, calledMethods);

        int coveredMethods = calledMethods.size() + this.removedMethods.size();
        int missingMethods = this.totalMethods - coveredMethods;
        assert (this.totalMethods == coveredMethods + missingMethods);
        fitness = 1.0 * missingMethods;

        printStatusMessages(suite, totalMethods - missingMethods, fitness);

        if (totalMethods > 0)
            suite.setCoverage(this, (double) coveredMethods / (double) totalMethods);
        else
            suite.setCoverage(this, 1.0);

        suite.setNumOfCoveredGoals(this, coveredMethods);

        if (hasTimeoutOrTestException) {
            logger.info("Test suite has timed out, setting fitness to max value " + totalMethods);
            fitness = totalMethods;
            //suite.setCoverage(0.0);
        }

        updateIndividual(suite, fitness);

        assert (coveredMethods <= totalMethods) : "Covered " + coveredMethods + " vs total goals " + totalMethods;
        assert (fitness >= 0.0);
        assert (fitness != 0.0 || coveredMethods == totalMethods) : "Fitness: " + fitness + ", "
                + "coverage: " + coveredMethods + "/" + totalMethods;
        assert (suite.getCoverage(this) <= 1.0) && (suite.getCoverage(this) >= 0.0) : "Wrong coverage value "
                + suite.getCoverage(this);

        String path = "/home/afonso/IdeaProjects/gfg.txt";
        try {
            new fileWriter(fitness+"", path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fitness;
    }

    /**
     * Some useful debug information
     *
     * @param coveredMethods
     * @param fitness
     */
    protected void printStatusMessages(TestSuiteChromosome suite,
                                       int coveredMethods, double fitness) {
        if (coveredMethods > maxCoveredMethods) {
            logger.info("(Methods) Best individual covers " + coveredMethods + "/"
                    + totalMethods + " methods");
            maxCoveredMethods = coveredMethods;
            logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
                    + suite.totalLengthOfTestCases());

        }
        if (fitness < bestFitness) {
            logger.info("(Fitness) Best individual covers " + coveredMethods + "/"
                    + totalMethods + " methods");
            bestFitness = fitness;
            logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
                    + suite.totalLengthOfTestCases());

        }
    }

    @Override
    public boolean updateCoveredGoals() {
        if (!Properties.TEST_ARCHIVE) {
            return false;
        }

        for (String method : this.toRemoveMethods) {
            TestFitnessFunction f = this.methodCoverageMap.remove(method);
            if (f != null) {
                this.removedMethods.add(method);
            } else {
                throw new IllegalStateException("Goal to remove not found: " + method + ", candidates: " + methodCoverageMap.keySet());
            }
        }

        this.toRemoveMethods.clear();
        logger.info("Current state of archive: " + Archive.getArchiveInstance().toString());

        assert this.totalMethods == this.methodCoverageMap.size() + this.removedMethods.size();

        return true;
    }
}
