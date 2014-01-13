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
package org.ngrinder.model;

import java.util.Comparator;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;

/**
 * Tag class for categorization of {@link PerfTest}.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "TAG")
public class Tag extends BaseModel<Tag> implements Comparator<Tag>, Comparable<Tag> {

	/**
	 * UUID.
	 */
	private static final long serialVersionUID = -1;

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags")
	private Set<PerfTest> perfTests;

	/**
	 * Tag value.
	 */
	private String tagValue;

	/**
	 * Default constructor.
	 */
	public Tag() {
	}

	/**
	 * Constructor.
	 *
	 * @param tagValue tag value
	 */
	public Tag(String tagValue) {
		this.tagValue = StringUtils.trimToEmpty(tagValue);

	}

	public String getTagValue() {
		return tagValue;
	}

	public void setTagValue(String tagValue) {
		this.tagValue = tagValue;
	}

	public Set<PerfTest> getPerfTests() {
		return perfTests;
	}

	public void setPerfTests(Set<PerfTest> perfTests) {
		this.perfTests = perfTests;
	}

	@Override
	public int compare(Tag o1, Tag o2) {
		return o1.tagValue.compareTo(o2.getTagValue());
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Tag && StringUtils.equalsIgnoreCase(this.getTagValue(), ((Tag) obj).getTagValue());
	}

	@Override
	public int hashCode() {
		return StringUtils.trimToEmpty(this.getTagValue()).hashCode();
	}

	@Override
	public int compareTo(Tag o) {
		return compare(this, o);
	}

}
