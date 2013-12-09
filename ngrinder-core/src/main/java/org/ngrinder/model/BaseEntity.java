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

import com.google.gson.annotations.Expose;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.persistence.*;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.ngrinder.common.util.ExceptionUtils.processException;

/**
 * Base Entity. This has a long type ID field
 *
 * @param <M> wrapped entity type
 * @author Liu Zhifei
 * @author JunHo Yoon
 * @since 3.0
 */
@MappedSuperclass
public class BaseEntity<M> implements Serializable {

	private static final long serialVersionUID = 8571113820348514692L;

	@Expose
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false, insertable = true, updatable = false)
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	/**
	 * This function is used to check whether the entity id exist. It is not
	 * used to check the entity existence in DB. It can be used to check the
	 * entity in controller, which is passed from page.
	 *
	 * @return true if exists
	 */
	public boolean exist() {
		return id != null && id != 0;
	}

	/**
	 * Merge source entity into current entity.
	 *
	 * Only not null value is merged.
	 *
	 * @param source merge source
	 * @return merged entity
	 */
	@SuppressWarnings("unchecked")
	public M merge(M source) {
		Field forDisplay = null;
		try {
			Field[] fields = getClass().getDeclaredFields();
			// Iterate over all the attributes
			for (Field each : fields) {
				if (each.isSynthetic()) {
					continue;
				}
				final int modifiers = each.getModifiers();
				if (Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers)) {
					continue;
				}
				forDisplay = each;
				if (!each.isAccessible()) {
					each.setAccessible(true);
				}
				final Object value = each.get(source);
				if (value != null) {
					each.set(this, value);
				}
			}
			return (M) this;
		} catch (Exception e) {
			String displayName = (forDisplay == null) ? "Empty" : forDisplay.getName();
			throw processException(displayName + " - Exception occurred while merging an entity from " + source + " to "
					+ this, e);
		}
	}


	/**
	 * Clone current entity.
	 *
	 * Only not null value is merged.
	 *
	 * @param toInstance instance to which the value is copied.
	 * @return cloned entity
	 */
	public M cloneTo(M toInstance) {
		Field forDisplay = null;
		try {
			Field[] fields = getClass().getDeclaredFields();
			// Iterate over all the attributes
			for (Field each : fields) {
				if (each.isSynthetic()) {
					continue;
				}
				final int modifiers = each.getModifiers();
				if (Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers)) {
					continue;
				}
				forDisplay = each;
				final Cloneable annotation = each.getAnnotation(Cloneable.class);
				if (annotation == null) {
					continue;
				}
				if (!each.isAccessible()) {
					each.setAccessible(true);
				}
				each.set(toInstance, each.get(this));
			}
			return toInstance;
		} catch (Exception e) {
			String displayName = (forDisplay == null) ? "Empty" : forDisplay.getName();
			throw processException(displayName + " - Exception occurred while cloning an entity from " + this + " to "
					+ toInstance, e);
		}
	}
}
