/*******************************************************************************
 * Copyright (c) 2019 Urs Zeidler.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.veo.persistence.entity.jpa.groups;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.veo.core.entity.Process;
import org.veo.core.entity.groups.ProcessGroup;
import org.veo.persistence.entity.jpa.ProcessData;

@Entity(name = "process_group")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class ProcessGroupData extends ProcessData
        implements ProcessGroup, EntityLayerSupertypeGroupData<ProcessData> {

    @ManyToMany(targetEntity = ProcessData.class,
                cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "process_group_members",
               joinColumns = @JoinColumn(name = "group_id"),
               inverseJoinColumns = @JoinColumn(name = "member_id"))
    private Set<Process> members;

    @Override
    public Set<Process> getMembers() {
        return members;
    }

    @Override
    public void setMembers(Set<Process> members) {
        this.members = members;
    }
}
