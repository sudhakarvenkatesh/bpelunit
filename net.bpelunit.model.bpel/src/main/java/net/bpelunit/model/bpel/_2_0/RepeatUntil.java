package net.bpelunit.model.bpel._2_0;

import net.bpelunit.model.bpel.IActivityContainer;
import net.bpelunit.model.bpel.IRepeatUntil;

import org.oasisOpen.docs.wsbpel.x20.process.executable.TRepeatUntil;

public class RepeatUntil extends AbstractSingleContainer<TRepeatUntil>
		implements IRepeatUntil {

	public RepeatUntil(TRepeatUntil wrappedRepeatUntil, IActivityContainer parent) {
		super(wrappedRepeatUntil, parent);
	}
}
