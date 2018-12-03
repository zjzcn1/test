package com.zjzcn.test.control.transport;

import java.util.List;

public interface Codec<M, B> {

    void encode(M in, B out);

	void decode(B in, List<M> out);

}
