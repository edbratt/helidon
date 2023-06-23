/*
 * Copyright (c) 2022, 2023 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.builder.test.testsubjects;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Example of what would be code generated by the builder. Used in testing.
 */
@SuppressWarnings("unchecked")
public class Level0ManualImpl<T extends Level0ManualImpl> implements Level0, Supplier<T> {
    private final String level0StringAttribute;

    protected Level0ManualImpl(Builder builder) {
        this.level0StringAttribute = builder.level0StringAttribute;
    }

    /**
     * Used for testing purposes only.
     *
     * @return ignored, here for testing only
     */
    @Override
    public T get() {
        return (T) this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + toStringInner() + ")";
    }

    @Override
    public boolean equals(Object another) {
        if (this == another) {
            return true;
        }
        if (!(another instanceof Level0)) {
            return false;
        }
        Level0 other = (Level0) another;
        boolean equals = true;
        equals &= Objects.equals(getLevel0StringAttribute(), other.getLevel0StringAttribute());
        return equals;
    }

    /*
    // builder base - nothing
    // builder
    Annotation.Builder{
      typeName=io.helidon.builder.Prototype.Blueprint
      values={builderInterceptor=....}
    }
    // impl
    Annotation{
      typeName=io.helidon.builder.Prototype.Blueprint
      values={builderInterceptor=....}
    }

    inheritance
    Qualifier{}Annotation{typeName=}
     */

    protected CharSequence toStringInner() {
        return "level0StringAttribute=" + getLevel0StringAttribute();
    }

    /**
     * Used for testing purposes only.
     *
     * @return ignored, here for testing only
     */
    @Override
    public String getLevel0StringAttribute() {
        return level0StringAttribute;
    }

    /**
     * Used for testing purposes only.
     *
     * @return ignored, here for testing only
     */
    public static Builder<? extends Builder, ? extends Level0> builder() {
        return new Builder(null);
    }

    /**
     * Used for testing purposes only.
     *
     * @return ignored, here for testing only
     */
    public static Builder<? extends Builder, ? extends Level0> toBuilder(Level0 val) {
        return new Builder(val);
    }


    /**
     * Used for testing purposes only.
     */
    public static class Builder<B extends Builder<B, T>, T extends Level0> implements Supplier<T>, Consumer<T> {
        private String level0StringAttribute = "1";

        protected Builder(T val) {
//            accept(val);
            acceptThis(val);
        }

        protected B identity() {
            return (B) this;
        }

        /**
         * Used for testing purposes only.
         *
         * @return ignored, here for testing only
         */
        @Override
        public T get() {
            return (T) build();
        }

        /**
         * Used for testing purposes only.
         *
         * @return ignored, here for testing only
         */
        @Override
        public void accept(T val) {
            // super.accept(val);
            acceptThis(val);
        }

        /**
         * Used for testing purposes only.
         *
         * @return ignored, here for testing only
         */
        private void acceptThis(T val) {
            if (val == null) {
                return;
            }

            this.level0StringAttribute = val.getLevel0StringAttribute();
        }

        /**
         * Used for testing purposes only.
         *
         * @return ignored, here for testing only
         */
        public B update(Consumer<T> consumer) {
            consumer.accept(get());
            return identity();
        }

        /**
         * Used for testing purposes only.
         *
         * @return ignored, here for testing only
         */
        public B level0StringAttribute(String val) {
            this.level0StringAttribute = val;
            return identity();
        }

        /**
         * Used for testing purposes only.
         *
         * @return ignored, here for testing only
         */
        public Level0ManualImpl build() {
            return new Level0ManualImpl(this);
        }
    }

    /**
     * Used for testing purposes only.
     */
    public static class Bldr extends Builder<Bldr, Level0> {
        protected Bldr(Level0 val) {
            super(val);
        }
    }

}
