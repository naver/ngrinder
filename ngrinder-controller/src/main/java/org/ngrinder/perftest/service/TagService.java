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
package org.ngrinder.perftest.service;

import static org.ngrinder.perftest.repository.TagSpecification.hasPerfTest;
import static org.ngrinder.perftest.repository.TagSpecification.isStartWith;
import static org.ngrinder.perftest.repository.TagSpecification.lastModifiedOrCreatedBy;
import static org.ngrinder.perftest.repository.TagSpecification.valueIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Tag;
import org.ngrinder.model.User;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.ngrinder.perftest.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tag Service. Tag support which is used to categorize {@link PerfTest}
 * 
 * @author JunHo Yoon
 * @since 3.0
 * 
 */
@Service
public class TagService {

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private PerfTestRepository perfTestRepository;

	/**
	 * Add tags.
	 * 
	 * @param user user
	 * @param tags tag string list
	 * @return inserted tags
	 */
	@Transactional
	public SortedSet<Tag> addTags(User user, String[] tags) {
		if (ArrayUtils.isEmpty(tags)) {
			return new TreeSet<Tag>();
		}

		Specifications<Tag> spec = Specifications.where(lastModifiedOrCreatedBy(user)).and(valueIn(tags));
		List<Tag> foundTags = tagRepository.findAll(spec);
		SortedSet<Tag> allTags = new TreeSet<Tag>(foundTags);
		for (String each : tags) {
			Tag newTag = new Tag(StringUtils.trimToEmpty(StringUtils.replace(each, ",", "")));
			if (allTags.contains(newTag)) {
				continue;
			}
			if (!foundTags.contains(newTag) && !allTags.contains(newTag)) {
				allTags.add(saveTag(user, newTag));
			}
		}
		return allTags;
	}

	/**
	 * Get all tags which belongs to given user and start with given string.
	 * 
	 * @param user 		user
	 * @param startWith	string
	 * @return found tags
	 */
	public List<Tag> getAllTags(User user, String startWith) {
		Specifications<Tag> spec = Specifications.where(hasPerfTest());
		spec = spec.and(lastModifiedOrCreatedBy(user));
		if (StringUtils.isNotBlank(startWith)) {
			spec = spec.and(isStartWith(StringUtils.trimToEmpty(startWith)));
		}
		return tagRepository.findAll(spec);
	}

	/**
	 * Get all tags which belongs to given user and start with given string.
	 * 
	 * @param user	user
	 * @param query	query string
	 * @return found tag string lists
	 */
	public List<String> getAllTagStrings(User user, String query) {
		List<String> allString = new ArrayList<String>();
		for (Tag each : getAllTags(user, query)) {
			allString.add(each.getTagValue());
		}
		Collections.sort(allString);
		return allString;
	}

	/**
	 * Save Tag. Because this method can be called in {@link TagService} internally, so created user
	 * / data should be set directly.
	 * 
	 * @param user 	user
	 * @param tag	tag
	 * @return saved {@link Tag} instance
	 */
	public Tag saveTag(User user, Tag tag) {
		Date createdDate = new Date();
		if (tag.getCreatedUser() == null) {
			tag.setCreatedUser(user);
			tag.setCreatedDate(createdDate);
		}
		tag.setLastModifiedUser(user);
		tag.setLastModifiedDate(createdDate);
		return tagRepository.save(tag);
	}

	/**
	 * Delete a tag.
	 * 
	 * @param user	user
	 * @param tag	tag
	 */
	@Transactional
	public void deleteTag(User user, Tag tag) {
		for (PerfTest each : tag.getPerfTests()) {
			each.getTags().remove(tag);
		}
		perfTestRepository.save(tag.getPerfTests());
		tagRepository.delete(tag);
	}

	/**
	 * Delete all tags belonging to given user.
	 * 
	 * @param user	user
	 */
	@Transactional
	public void deleteTags(User user) {
		Specifications<Tag> spec = Specifications.where(lastModifiedOrCreatedBy(user));
		List<Tag> userTags = tagRepository.findAll(spec);
		for (Tag each : userTags) {
			Set<PerfTest> perfTests = each.getPerfTests();
			if (perfTests != null) {
				for (PerfTest eachPerfTest : perfTests) {
					eachPerfTest.getTags().remove(each);
				}
				perfTestRepository.save(each.getPerfTests());
			}
		}
		tagRepository.delete(userTags);
	}
}
