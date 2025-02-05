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
package org.evosuite.coverage.executionTime;

import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

import java.util.List;

public class ExecutionTimeSuiteFitness extends TestSuiteFitnessFunction {


    private static float biggestExecutionTime = 0;

    @Override
    public double getFitness(TestSuiteChromosome suite) {

        List<ExecutionResult> results = runTestSuite(suite);

        long SuiteExecutionTime = 0;
        for (ExecutionResult result : results) {
            if (result.hasTimeout()) {
                continue;
            }
            SuiteExecutionTime += result.getExecutionTime();
        }
        if (SuiteExecutionTime>biggestExecutionTime){
            biggestExecutionTime = SuiteExecutionTime;
        }
        float fitness = 1;
        if (biggestExecutionTime!=0){
            fitness = 1- SuiteExecutionTime/biggestExecutionTime;
        }
        updateIndividual(suite, fitness);
        return fitness;
    }

    //@Override
    //public boolean isMaximizationFunction() {
    //    return false;
   // }
}
