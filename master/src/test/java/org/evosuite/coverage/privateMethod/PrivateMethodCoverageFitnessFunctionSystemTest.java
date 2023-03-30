package org.evosuite.coverage.privateMethod;

import com.examples.with.different.packagename.reflection.PrivateMixAfonsoTest;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.List;

public class PrivateMethodCoverageFitnessFunctionSystemTest extends SystemTestBase {


    private static final Criterion[] defaultCriterion = Properties.CRITERION;

    private static final boolean defaultArchive = Properties.TEST_ARCHIVE;

    @After
    public void resetProperties() {
        Properties.CRITERION = defaultCriterion;
        Properties.TEST_ARCHIVE = defaultArchive;
        int a = 0;
    }

    @Before
    public void beforeTest() {
        Properties.CRITERION[0] = Criterion.PRIVATEMETHOD;
        Properties.MINIMIZE = true; // THIS MAXIMIZES IF SET TO FALSE?????????
        //int a = 0;
    }

    @Test
    public void testPrivateMethodFitnessSimpleExampleWithArchive() {
        Properties.TEST_ARCHIVE = true;
        systemTestPrivateMethodCoverageInnerClasses();
    }

    @Test
    public void testPrivateMethodFitnessSimpleExampleWithoutArchive() {
        Properties.TEST_ARCHIVE = false;
        systemTestPrivateMethodCoverageInnerClasses();
    }


    @Test
    public void systemTestPrivateMethodCoverageInnerClasses() {
        //Properties.CRITERION = new Properties.Criterion[]{Properties.Criterion.PRIVATEMETHOD};
      //  Properties.CLIENT_ON_THREAD = true;
        EvoSuite evosuite = new EvoSuite();
        String targetClass = PrivateMixAfonsoTest.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION[0] = Criterion.PRIVATEMETHOD;
        Properties.TEST_ARCHIVE = true;
        //Properties.COVERAGE_MATRIX = true;
        Properties.MINIMIZE = true;


        //java -jar evosuite-master-1.2.1-SNAPSHOT.jar -class tutorial.Stack -projectCP target/classes
        String[] command = new String[]{"-generateSuite", "-class", targetClass, "-criterion=PRIVATEMETHOD", "-Dalgorithm=MONOTONIC_GA", "-Doutput_variables=configuration_id,TARGET_CLASS,criterion,Size,Length,BranchCoverageTimeline,CoverageTimeline" ,"-Dtimeline_interval=500"};// "-Dalgorithm=DYNAMOSA"};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
       List<TestSuiteChromosome> bestSet = ga.getBestIndividuals();
        TestSuiteChromosome best = ga.getBestIndividual();

        int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size();

        Assert.assertEquals(4, goals);
        System.out.println("All objectives achieved!");
       System.out.println("EvolvedTestSuite:\n" + best);
      // double i = best.getCoverage();
      // Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }
}
