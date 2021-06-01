/*******************************************************************************
 * verinice.veo
 * Copyright (C) 2021  Urs Zeidler.
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
package org.veo.core.entity;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CatalogItem The catalog item contains an element and/other related catalog
 * item. It describes currently two different abstract use cases: 1. Apply the
 * containt catalogable: defined by KEa.1 and KEa.2 2. Update an entity from an
 * old to a new version of the domainTemplate to the new one. Usecase 1 is
 * defined by the catalogable and a set of TailoringReferences. Usecase 2 is
 * defined by a set of UpdateReferences.
 */
public interface CatalogItem extends ElementOwner {
    String SINGULAR_TERM = "catalogitem";
    String PLURAL_TERM = "catalogitems";

    Comparator<? super CatalogItem> BY_CATALOGITEMS = (ci1, ci2) -> ci1.getId()
                                                                       .uuidValue()
                                                                       .compareTo(ci2.getId()
                                                                                     .uuidValue());

    /**
     * Includes itself together with {@link this.getElementsToCreate()}. This list
     * is ordered. The item itself is at the first position.
     */
    default List<CatalogItem> getAllElementsToCreate() {
        List<CatalogItem> result = this.getElementsToCreate()
                                       .stream()
                                       .sorted(BY_CATALOGITEMS)
                                       .distinct()
                                       .collect(Collectors.toList());
        result.add(0, this);
        return result;
    }

    /**
     * Return the set additional elements to create. These elements are defined by
     * {@link TailoringReference} of type {@link TailoringReferenceType#COPY} or
     * {@link TailoringReferenceType#COPY_ALWAYS}.
     */
    default Set<CatalogItem> getElementsToCreate() {
        Set<CatalogItem> elementsToCreate = new HashSet<>();
        this.getTailoringReferences()
            .stream()
            .filter(TailoringReferenceTyped.IS_COPY_PREDICATE)
            .forEach(r -> addElementsToCopy(r, elementsToCreate));
        return elementsToCreate;
    }

    default void addElementsToCopy(TailoringReference reference, Set<CatalogItem> itemList) {
        itemList.add(reference.getCatalogItem());
        reference.getCatalogItem()
                 .getTailoringReferences()
                 .stream()
                 .filter(TailoringReferenceTyped.IS_COPY_PREDICATE)
                 .forEach(rr -> addElementsToCopy(rr, itemList));
    }

    /**
     * The owner of this is a catalog.
     */
    Catalog getCatalog();

    void setCatalog(Catalog aCatalog);

    /**
     * All the tailoring references for this catalog item.
     */
    Set<TailoringReference> getTailoringReferences();

    default void setTailoringReferences(Set<TailoringReference> tailoringReferences) {
        getTailoringReferences().clear();
        tailoringReferences.forEach(tailoringReference -> tailoringReference.setOwner(this));
        getTailoringReferences().addAll(tailoringReferences);
    }

    /**
     * The catalogable is the template element which will applied. A copy of the
     * object will be inserted.
     */
    Catalogable getElement();

    void setElement(Catalogable aCatalogable);

    /**
     * All the update refreneces for this catalog item.
     */
    Set<UpdateReference> getUpdateReferences();

    default void setUpdateReferences(Set<UpdateReference> updateReferences) {
        getUpdateReferences().clear();
        updateReferences.forEach(updateReference -> updateReference.setOwner(this));
        getUpdateReferences().addAll(updateReferences);
    }

    String getNamespace();

    void setNamespace(String aNamespace);

    @Override
    default Class<? extends Identifiable> getModelInterface() {
        return CatalogItem.class;
    }

    @Override
    default String getModelType() {
        return SINGULAR_TERM;
    }

    default String getDisplayName() {
        return getElement().getDisplayName();
    }

    default Optional<Client> getOwningClient() {
        return Optional.ofNullable(getCatalog())
                       .flatMap(Catalog::getOwningClient);
    }
}
