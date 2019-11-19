package org.ngrinder.common.controller.annotation;

import java.lang.annotation.*;

/**
 * Annotation which marks the required global model attributes.
 *
 * @since 3.5.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GlobalControllerModel {
}
