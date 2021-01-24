package io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mapping {

    String value();

    MapType map() default MapType.AUTO;
}
