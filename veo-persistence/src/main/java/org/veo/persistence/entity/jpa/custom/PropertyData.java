/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2020  Urs Zeidler.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.veo.persistence.entity.jpa.custom;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.validation.constraints.NotNull;

import org.veo.core.entity.CustomProperties;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@IdClass(PropertyData.PropertyId.class)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class PropertyData {
    @Id
    protected String key;

    @Id
    protected String parentId;

    public PropertyData(String key, String value) {
        this.key = key;
        this.stringValue = value;
        this.type = Type.STRING;
    }

    public PropertyData(String key, Double value) {
        this.key = key;
        this.doubleValue = value;
        this.type = Type.DOUBLE;
    }

    public PropertyData(String key, Integer value) {
        this.key = key;
        this.integerValue = value;
        this.type = Type.INTEGER;
    }

    public PropertyData(String key, Boolean value) {
        this.key = key;
        this.booleanValue = value;
        this.type = Type.BOOLEAN;
    }

    public PropertyData(String key, OffsetDateTime value) {
        this.key = key;
        this.offsetDateTimeValue = value;
        this.type = Type.OFFSET_DATE_TIME;
    }

    public PropertyData(String key, List<String> value) {
        this.key = key;
        this.type = Type.STRING_LIST;
        this.stringListValue.addAll(value);
    }

    @NotNull
    @Column(nullable = false)
    private Type type;

    @Column(length = CustomProperties.MAXIMUM_STRING_LENGTH)
    private String stringValue;

    private Boolean booleanValue;

    private Double doubleValue;

    private Integer integerValue;

    private OffsetDateTime offsetDateTimeValue;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "propertydata_stringvalue",
                     joinColumns = {
                             @JoinColumn(name = "propertydata_key", referencedColumnName = "key"),
                             @JoinColumn(name = "propertydata_parent_id",
                                         referencedColumnName = "parentid") })
    @OrderColumn(name = "index_id")
    private List<String> stringListValue = new ArrayList<>();

    public enum Type {
        BOOLEAN, STRING, STRING_LIST, DOUBLE, INTEGER, OFFSET_DATE_TIME
    }

    @Data
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class PropertyId implements Serializable {
        private String key;
        private String parentId;
    }

    public Object getValue() {
        switch (type) {
        case STRING:
            return getStringValue();
        case STRING_LIST:
            return getStringListValue();
        case DOUBLE:
            return getDoubleValue();
        case INTEGER:
            return getIntegerValue();
        case BOOLEAN:
            return getBooleanValue();
        case OFFSET_DATE_TIME:
            return getOffsetDateTimeValue();
        default:
            throw new UnsupportedOperationException("Unhandled property type: " + type);
        }
    }

    public void setStringListValue(List<String> newValues) {
        stringListValue.clear();
        stringListValue.addAll(newValues);
    }

}
