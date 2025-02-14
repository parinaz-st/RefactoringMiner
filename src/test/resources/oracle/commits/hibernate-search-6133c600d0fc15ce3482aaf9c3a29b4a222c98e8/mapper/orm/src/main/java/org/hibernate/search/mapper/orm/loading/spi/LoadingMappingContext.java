/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.orm.loading.spi;

import jakarta.persistence.EntityManager;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.search.mapper.orm.search.loading.EntityLoadingCacheLookupStrategy;

public interface LoadingMappingContext {

	EntityLoadingCacheLookupStrategy cacheLookupStrategy();

	int fetchSize();

	SessionFactoryImplementor sessionFactory();

	LoadingSessionContext sessionContext(EntityManager entityManager);

}
