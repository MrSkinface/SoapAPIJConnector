package org.exite.beans.soap;

import lombok.Builder;
import lombok.Data;

/**
 * Created by levitskym on 18.09.19
 */
@Data
@Builder
public class Content {

    private byte[] body;
    private byte[] sign;
}
