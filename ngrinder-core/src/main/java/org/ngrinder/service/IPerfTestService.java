/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.service;

import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.model.User;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * {@link PerfTest} service interface. This is visible from plugin.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public interface IPerfTestService {

	/**
	 * get test detail.
	 * 
	 * @param user	current operation user.
	 * @param id	test id
	 * @return perftest	{@link PerfTest}
	 */
	public abstract PerfTest getOne(User user, Long id);

	/**
	 * Get {@link PerfTest} list created within the given time frame.
	 * 
	 * @param start	start time.
	 * @param end	end time.
	 * @return found {@link PerfTest} list
	 */
	public abstract List<PerfTest> getAll(Date start, Date end);

	/**
	 * Get {@link PerfTest} list created within the given time frame and region name.
	 * 
	 * @param start		start time.
	 * @param end		end time.
	 * @param region	region
	 * @return found {@link PerfTest} list
	 */
	public abstract List<PerfTest> getAll(Date start, Date end, String region);

	/**
	 * Get {@link PerfTest} list of some IDs.
	 * 
	 * @param user	current operation user
	 * @param ids	test IDs, which is in format: "1,3,6,11"
	 * @return perftest list test list of those IDs
	 */
	public abstract List<PerfTest> getAll(User user, Long[] ids);

	/**
	 * Get PerfTest count which have given status.
	 * 
	 * @param user		user who created test. null to retrieve all
	 * @param statuses	status set
	 * @return the count
	 */
	public abstract long count(User user, Status[] statuses);

	/**
	 * Get {@link PerfTest} list which have give state.
	 * 
	 * @param user		user who created {@link PerfTest}. if null, retrieve all test
	 * @param statuses	set of {@link Status}
	 * @return found {@link PerfTest} list.
	 */
	public abstract List<PerfTest> getAll(User user, Status[] statuses);

	/**
	 * Save {@link PerfTest}. This function includes logic the updating script revision when it's
	 * READY status.
	 * 
	 * @param user		user
	 * @param perfTest	{@link PerfTest} instance to be saved.
	 * @return Saved {@link PerfTest}
	 */
	public abstract PerfTest save(User user, PerfTest perfTest);

	/**
	 * Get PerfTest by testId.
	 * 
	 * @param testId	PerfTest id
	 * @return found {@link PerfTest}, null otherwise
	 */
	public abstract PerfTest getOne(Long testId);

	/**
	 * Get PerfTest with tag infos by testId.
	 * 
	 * @param testId	PerfTest id
	 * @return found {@link PerfTest}, null otherwise
	 */
	public abstract PerfTest getOneWithTag(Long testId);

	/**
	 * Get currently testing PerfTest.
	 * 
	 * @return found {@link PerfTest} list
	 */
	public abstract List<PerfTest> getAllTesting();

	/**
	 * Get PerfTest Directory in which the distributed file is stored.
	 * 
	 * @param perfTest	pefTest from which distribution directory calculated
	 * @return path on in files are saved.
	 */
	public abstract File getDistributionPath(PerfTest perfTest);

	/**
	 * Get perf test base directory.
	 * 
	 * @param perfTest	perfTest
	 * @return directory prefTest base path
	 */
	public abstract File getPerfTestDirectory(PerfTest perfTest);

	/**
	 * Get all perf test list.
	 * 
	 * Note : This is only for test
	 * 
	 * @return all {@link PerfTest} list
	 * 
	 */
	public abstract List<PerfTest> getAllPerfTest();

	/**
	 * Mark Stop on {@link PerfTest}.
	 * 
	 * @param user	user
	 * @param id	perftest id
	 */
	public abstract void stop(User user, Long id);

	/**
	 * Return stop requested test.
	 * 
	 * @return stop requested perf test
	 */
	public abstract List<PerfTest> getAllStopRequested();

	/**
	 * Add comment on {@link PerfTest}.
	 * 
	 * @param user			current operated user
	 * @param testId		perftest id
	 * @param testComment	comment
	 * @param tagString		tagString
	 */
	public abstract void addCommentOn(User user, Long testId, String testComment, String tagString);

	/**
	 * Save performance test with given status.
	 * 
	 * This method is only used for changing {@link Status}
	 * 
	 * @param perfTest	{@link PerfTest} instance which will be saved.
	 * @param status	Status to be assigned
	 * @param message	progress message
	 * @return saved {@link PerfTest}
	 */
	public abstract PerfTest markStatusAndProgress(PerfTest perfTest, Status status, String message);

	/**
	 * Get performance test statistic path.
	 * 
	 * @param perfTest	perftest
	 * @return statistic path
	 */
	@SuppressWarnings("UnusedDeclaration")
	public abstract File getStatisticPath(PerfTest perfTest);

}