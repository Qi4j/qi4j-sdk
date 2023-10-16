/**
 * The Messaging Extension is to automatically connect Qi4j services to Messaging systems, such as RabbitMQ and Kafka.
 *
 * Example;
 * <pre><code>
 *  &#64;TopicTypeFactory( CustomerTypeFactory.class )
 *  &#64;TopicAlias("customers")
 *  public interface CustomerMaintenanceTopic
 *  {
 *     &#64;TopicFilter(UpdateFilter.class)
 *     void updateCustomer( Customer newCustomerInfo );
 *
 *     &#64;TopicFilter(DeleteFilter.class)
 *     void deleteCustomer( CustomerIdentity customerId );
 *
 *     &#64;TopicFilter(AddContactFilter.class)
 *     void addCustomerContact( CustomerIdentity customerId, ContactPerson contact );
 *
 *     &#64;TopicFilter(RemoveContactFilter.class)
 *     void removeCustomerContact( CustomerIdentity customerId, ContactPerson contact );
 *  }
 * </code></pre>
 *
 */
package org.qi4j.api.messaging;

