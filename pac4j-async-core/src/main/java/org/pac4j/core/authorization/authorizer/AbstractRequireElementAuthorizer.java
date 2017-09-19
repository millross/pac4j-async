package org.pac4j.core.authorization.authorizer;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public abstract class AbstractRequireElementAuthorizer <C extends WebContext<?>,E,  U extends CommonProfile> extends ProfileAuthorizer<C, U> {

    protected Set<E> elements;

    @Override
    public Boolean isAuthorized(final C context, final List<U> profiles) throws HttpAction {
        return isAnyAuthorized(context, profiles);
    }

    /**
     * Check a specific element.
     *
     * @param context the web context
     * @param profile the profile
     * @param element the element to check
     * @return whether it is authorized for this element
     * @throws HttpAction whether an additional HTTP action is required
     */
    protected abstract Boolean check(final C context, final U profile, final E element) throws HttpAction;

    public Set<E> getElements() {
        return elements;
    }

    public void setElements(final Set<E> elements) {
        this.elements = elements;
    }

    public void setElements(final List<E> elements) {
        if (elements != null) {
            this.elements = new HashSet<>(elements);
        }
    }

    public void setElements(final E... elements) {
        if (elements != null) {
            setElements(Arrays.asList(elements));
        }
    }
}
