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

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * Tag class for categorization of {@link PerfTest}.
 *
 * @since 3.0
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@Getter
@Setter
@Entity
@Table(name = "TAG")
public class Tag extends BaseModel<Tag> implements Comparable<Tag> {

	/**
	 * UUID.
	 */
	private static final long serialVersionUID = -1;

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags")
	private Set<PerfTest> perfTests;

	private String tagValue;

	public Tag() {
	}

	public Tag(String tagValue) {
		this.tagValue = StringUtils.trimToEmpty(tagValue);
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
	public int compareTo(Tag other) {
		return this.tagValue.compareTo(other.getTagValue());
	}

}
