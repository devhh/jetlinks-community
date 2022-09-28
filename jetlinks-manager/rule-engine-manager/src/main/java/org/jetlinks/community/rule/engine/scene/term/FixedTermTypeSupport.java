package org.jetlinks.community.rule.engine.scene.term;

import lombok.Getter;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.NativeSql;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.PrepareSqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.SqlFragments;
import org.hswebframework.web.i18n.LocaleUtils;
import org.jetlinks.core.metadata.DataType;
import org.jetlinks.core.metadata.types.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
public enum FixedTermTypeSupport implements TermTypeSupport {

    eq("等于", "eq"),
    neq("不等于", "neq"),

    gt("大于", "gt", DateTimeType.ID, IntType.ID, LongType.ID, FloatType.ID, DoubleType.ID),
    gte("大于等于", "gte", DateTimeType.ID, IntType.ID, LongType.ID, FloatType.ID, DoubleType.ID),
    lt("小于", "lt", DateTimeType.ID, IntType.ID, LongType.ID, FloatType.ID, DoubleType.ID),
    lte("小于等于", "lte", DateTimeType.ID, IntType.ID, LongType.ID, FloatType.ID, DoubleType.ID),

    btw("在...之间", "btw", DateTimeType.ID, IntType.ID, LongType.ID, FloatType.ID, DoubleType.ID) {
        @Override
        protected Object convertValue(Object val) {
            return val;
        }
    },
    nbtw("不在...之间", "nbtw", DateTimeType.ID, IntType.ID, LongType.ID, FloatType.ID, DoubleType.ID) {
        @Override
        protected Object convertValue(Object val) {
            return val;
        }
    },
    in("在...之中", "in", StringType.ID, IntType.ID, LongType.ID, FloatType.ID, DoubleType.ID, EnumType.ID, ArrayType.ID) {
        @Override
        protected Object convertValue(Object val) {
            return val;
        }
    },
    nin("不在...之中", "not in", StringType.ID, IntType.ID, LongType.ID, FloatType.ID, DoubleType.ID, EnumType.ID, ArrayType.ID) {
        @Override
        protected Object convertValue(Object val) {
            return val;
        }
    },

    like("包含字符", "str_like", StringType.ID),
    nlike("不包含字符", "not str_like", StringType.ID),

    ;

    private final String text;
    private final Set<String> supportTypes;

    private final String function;

    private FixedTermTypeSupport(String text, String function, String... supportTypes) {
        this.text = text;
        this.function = function;
        this.supportTypes = new HashSet<>(Arrays.asList(supportTypes));
    }

    @Override
    public boolean isSupported(DataType type) {
        return supportTypes.isEmpty() || supportTypes.contains(type.getType());
    }

    protected Object convertValue(Object val) {
        if (val instanceof Collection) {
            //值为数组,则尝试获取第一个值
            if (((Collection<?>) val).size() == 1) {
                return ((Collection<?>) val).iterator().next();
            }
        }
        return val;
    }

    @Override
    public final SqlFragments createSql(String column, Object value) {
        PrepareSqlFragments fragments = PrepareSqlFragments.of();
        fragments.addSql(function + "(", column, ",");
        if (value instanceof NativeSql) {
            fragments
                .addSql(((NativeSql) value).getSql())
                .addParameter(((NativeSql) value).getParameters());
        } else {
            fragments.addSql("?")
                     .addParameter(convertValue(value));
        }
        fragments.addSql(")");
        return fragments;
    }

    @Override
    public String getType() {
        return name();
    }

    @Override
    public String getName() {
        return LocaleUtils.resolveMessage("message.term_type_" + name(), text);
    }
}
