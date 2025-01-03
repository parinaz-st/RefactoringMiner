/*
   Copyright (c) 2014 LinkedIn Corp.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.linkedin.restli.internal.server.filter;


import com.linkedin.restli.internal.server.RestLiResponseEnvelope;
import com.linkedin.restli.server.filter.FilterResponseContext;


/**
 * @author nshankar
 */
public interface FilterResponseContextInternal extends FilterResponseContext
{
  /**
   * Set request data.
   *
   * @return Request data.
   */
  void setRestLiResponseEnvelope(RestLiResponseEnvelope data);

  /**
   * Get a reference to underlying {@link RestLiResponseDataInternal}.
   *
   * @return {@link RestLiResponseDataInternal}
   */
  RestLiResponseEnvelope getRestLiResponseEnvelope();
}
