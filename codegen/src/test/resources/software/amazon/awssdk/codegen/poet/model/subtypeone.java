package software.amazon.awssdk.services.jsonprotocoltests.model;

import java.util.Optional;
import javax.annotation.Generated;
import software.amazon.awssdk.annotations.SdkInternalApi;
import software.amazon.awssdk.core.protocol.ProtocolMarshaller;
import software.amazon.awssdk.core.protocol.StructuredPojo;
import software.amazon.awssdk.services.jsonprotocoltests.transform.SubTypeOneMarshaller;
import software.amazon.awssdk.utils.builder.CopyableBuilder;
import software.amazon.awssdk.utils.builder.ToCopyableBuilder;

/**
 */
@Generated("software.amazon.awssdk:codegen")
public class SubTypeOne implements StructuredPojo, ToCopyableBuilder<SubTypeOne.Builder, SubTypeOne> {
    private final String subTypeOneMember;

    private SubTypeOne(BuilderImpl builder) {
        this.subTypeOneMember = builder.subTypeOneMember;
    }

    /**
     * Returns the value of the SubTypeOneMember property for this object.
     *
     * @return The value of the SubTypeOneMember property for this object.
     */
    public String subTypeOneMember() {
        return subTypeOneMember;
    }

    @Override
    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    public static Class<? extends Builder> serializableBuilderClass() {
        return BuilderImpl.class;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = 31 * hashCode + ((subTypeOneMember() == null) ? 0 : subTypeOneMember().hashCode());
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SubTypeOne)) {
            return false;
        }
        SubTypeOne other = (SubTypeOne) obj;
        if (other.subTypeOneMember() == null ^ this.subTypeOneMember() == null) {
            return false;
        }
        if (other.subTypeOneMember() != null && !other.subTypeOneMember().equals(this.subTypeOneMember())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        if (subTypeOneMember() != null) {
            sb.append("SubTypeOneMember: ").append(subTypeOneMember()).append(",");
        }
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 1);
        }
        sb.append("}");
        return sb.toString();
    }

    public <T> Optional<T> getValueForField(String fieldName, Class<T> clazz) {
        switch (fieldName) {
        case "SubTypeOneMember":
            return Optional.of(clazz.cast(subTypeOneMember()));
        default:
            return Optional.empty();
        }
    }

    @SdkInternalApi
    @Override
    public void marshall(ProtocolMarshaller protocolMarshaller) {
        SubTypeOneMarshaller.getInstance().marshall(this, protocolMarshaller);
    }

    public interface Builder extends CopyableBuilder<Builder, SubTypeOne> {
        /**
         * Sets the value of the SubTypeOneMember property for this object.
         *
         * @param subTypeOneMember
         *        The new value for the SubTypeOneMember property for this object.
         * @return Returns a reference to this object so that method calls can be chained together.
         */
        Builder subTypeOneMember(String subTypeOneMember);
    }

    static final class BuilderImpl implements Builder {
        private String subTypeOneMember;

        private BuilderImpl() {
        }

        private BuilderImpl(SubTypeOne model) {
            subTypeOneMember(model.subTypeOneMember);
        }

        public final String getSubTypeOneMember() {
            return subTypeOneMember;
        }

        @Override
        public final Builder subTypeOneMember(String subTypeOneMember) {
            this.subTypeOneMember = subTypeOneMember;
            return this;
        }

        public final void setSubTypeOneMember(String subTypeOneMember) {
            this.subTypeOneMember = subTypeOneMember;
        }

        @Override
        public SubTypeOne build() {
            return new SubTypeOne(this);
        }
    }
}
