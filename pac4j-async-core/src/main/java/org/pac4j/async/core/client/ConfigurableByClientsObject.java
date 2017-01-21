package org.pac4j.async.core.client;

import org.pac4j.async.core.Named;
import org.pac4j.core.client.Clients;

/**
 * Interface to be implemented by clients which expect to have some configuration injected by a containing Clients
 * object. Previously in pac4j, there was code in the Clients class which tested the type of clients referred to
 * and then modified their configuration. This relationship is wrong, and in fact the client should be capable of
 * reading this configuration from the Clients object on demand (i.e. this is a clear case for inversion of control)
 * and this interface provides the mechanism by which that control will be inverted.
 *
 * Type parameter: C: the base class for clients to be used (generally AsyncClient or SyncClient). This will be a
 * type parameter on the client
 */
public interface ConfigurableByClientsObject<C extends Named & ConfigurableByClientsObject, A> {

    void configureFromClientsObject(Clients<C, A> toConfigureFrom);

}
