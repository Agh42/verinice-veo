/*******************************************************************************
 * Copyright (c) 2020 Urs Zeidler.
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
package org.veo.persistence.entity.jpa.transformer;

import java.util.HashSet;
import java.util.UUID;

import org.veo.core.entity.Asset;
import org.veo.core.entity.Client;
import org.veo.core.entity.Control;
import org.veo.core.entity.CustomLink;
import org.veo.core.entity.CustomProperties;
import org.veo.core.entity.Document;
import org.veo.core.entity.Domain;
import org.veo.core.entity.EntityLayerSupertype;
import org.veo.core.entity.GroupType;
import org.veo.core.entity.Key;
import org.veo.core.entity.ModelGroup;
import org.veo.core.entity.ModelObject;
import org.veo.core.entity.Person;
import org.veo.core.entity.Process;
import org.veo.core.entity.Unit;
import org.veo.core.entity.groups.AssetGroup;
import org.veo.core.entity.groups.ControlGroup;
import org.veo.core.entity.groups.DocumentGroup;
import org.veo.core.entity.groups.PersonGroup;
import org.veo.core.entity.groups.ProcessGroup;
import org.veo.core.entity.transform.EntityFactory;
import org.veo.persistence.entity.jpa.AssetData;
import org.veo.persistence.entity.jpa.ClientData;
import org.veo.persistence.entity.jpa.ControlData;
import org.veo.persistence.entity.jpa.CustomLinkData;
import org.veo.persistence.entity.jpa.CustomPropertiesData;
import org.veo.persistence.entity.jpa.DocumentData;
import org.veo.persistence.entity.jpa.DomainData;
import org.veo.persistence.entity.jpa.PersonData;
import org.veo.persistence.entity.jpa.ProcessData;
import org.veo.persistence.entity.jpa.UnitData;
import org.veo.persistence.entity.jpa.groups.AssetGroupData;
import org.veo.persistence.entity.jpa.groups.ControlGroupData;
import org.veo.persistence.entity.jpa.groups.DocumentGroupData;
import org.veo.persistence.entity.jpa.groups.PersonGroupData;
import org.veo.persistence.entity.jpa.groups.ProcessGroupData;

/**
 * @author urszeidler
 */
public class EntityDataFactory implements EntityFactory {

    @Override
    public Asset createAsset() {
        AssetData object = new AssetData();
        return object;
    }

    @Override
    public Client createClient() {
        return new ClientData();
    }

    @Override
    public Control createControl() {
        return new ControlData();
    }

    @Override
    public CustomLink createCustomLink() {
        return new CustomLinkData();
    }

    @Override
    public CustomProperties createCustomProperties() {
        return new CustomPropertiesData();
    }

    @Override
    public Document createDocument() {
        return new DocumentData();
    }

    @Override
    public Domain createDomain() {
        return new DomainData();
    }

    @Override
    public Person createPerson() {
        return new PersonData();
    }

    @Override
    public Process createProcess() {
        return new ProcessData();
    }

    @Override
    public Unit createUnit() {
        return new UnitData();
    }

    @Override
    public Person createPerson(Key<UUID> id, String name, Unit unit) {
        Person person = createPerson();
        setEntityLayerData(person, id, name, unit);
        return person;
    }

    @Override
    public Process createProcess(Key<UUID> id, String name, Unit unit) {
        Process process = createProcess();
        setEntityLayerData(process, id, name, unit);
        return process;
    }

    @Override
    public Client createClient(Key<UUID> id, String name) {
        Client client = createClient();
        client.setId(id);
        client.setName(name);
        client.setDomains(new HashSet<Domain>());
        return client;
    }

    @Override
    public Asset createAsset(Key<UUID> id, String name, Unit unit) {
        Asset asset = createAsset();
        setEntityLayerData(asset, id, name, unit);
        return asset;
    }

    @Override
    public Control createControl(Key<UUID> id, String name, Unit unit) {
        Control control = createControl();
        setEntityLayerData(control, id, name, unit);
        return control;
    }

    @Override
    public Unit createUnit(Key<UUID> id, String name, Unit parent) {
        Unit unit = createUnit();
        unit.setId(id);
        unit.setName(name);
        unit.setParent(parent);
        unit.setDomains(new HashSet<Domain>());
        return unit;
    }

    @Override
    public Document createDocument(Key<UUID> id, String name, Unit parent) {
        Document document = createDocument();
        setEntityLayerData(document, id, name, parent);
        return document;
    }

    @Override
    public Domain createDomain(Key<UUID> id, String name) {
        Domain domain = createDomain();
        domain.setId(id);
        domain.setName(name);
        return domain;
    }

    @Override
    public CustomLink createCustomLink(Key<UUID> id, String name, EntityLayerSupertype linkTarget,
            EntityLayerSupertype linkSource) {
        CustomLink link = createCustomLink();
        link.setId(id);
        link.setName(name);
        link.setTarget(linkTarget);
        link.setSource(linkSource);
        return link;
    }

    @Override
    public CustomProperties createCustomProperties(Key<UUID> id) {
        CustomProperties properties = createCustomProperties();
        properties.setId(id);
        return properties;
    }

    @Override
    public ModelGroup<?> createGroup(GroupType groupType) {
        return createGroupInstance(groupType.entityClass);
    }

    @Override
    public PersonGroup createPersonGroup() {
        return new PersonGroupData();
    }

    @Override
    public AssetGroup createAssetGroup() {
        return new AssetGroupData();
    }

    @Override
    public ProcessGroup createProcessGroup() {
        return new ProcessGroupData();
    }

    @Override
    public DocumentGroup createDocumentGroup() {
        return new DocumentGroupData();
    }

    @Override
    public ControlGroup createControlGroup() {
        return new ControlGroupData();
    }

    private void initCollection(EntityLayerSupertype object) {
        object.setDomains(new HashSet<Domain>());
        object.setLinks(new HashSet<CustomLink>());
        object.setCustomAspects(new HashSet<CustomProperties>());
    }

    private ModelGroup<?> createGroupInstance(Class<? extends ModelObject> entityClass) {
        if (entityClass == Person.class)// TODO: check does these come from the same classloader?
            return createPersonGroup();
        if (entityClass == Document.class)
            return createDocumentGroup();
        if (entityClass == Asset.class)
            return createAssetGroup();
        if (entityClass == Process.class)
            return createProcessGroup();
        if (entityClass == Control.class)
            return createControlGroup();
        throw new IllegalArgumentException("No such Group for: " + entityClass);
    }

    private void setEntityLayerData(EntityLayerSupertype entityLayerSupertype, Key<UUID> newUuid,
            String name, Unit unit) {
        entityLayerSupertype.setId(newUuid);
        entityLayerSupertype.setName(name);
        entityLayerSupertype.setOwner(unit);
        initCollection(entityLayerSupertype);
    }

}