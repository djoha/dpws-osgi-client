package fi.tut.fast.dpws.device;

public abstract class Event<O,F> extends OperationJAX<Void,O,F>{

	public Event(String name, String namespace, Class oClazz,
			Class fClazz) {
		super(name, namespace, Void.class, oClazz, fClazz);
		// TODO Auto-generated constructor stub
	}

}
