package fi.tut.fast.dpws.device.remote;

import fi.tut.fast.dpws.device.Operation;

public class OperationRef<I, O, F> extends Operation<I, O, F> {

	
	public OperationRef(String name, String namespace, Class<I> iClazz,
			Class<O> oClazz, Class<F> fClazz) {
		super(name, namespace, iClazz, oClazz, fClazz);
		// TODO Auto-generated constructor stub
	}

	@Override
	public O invoke(I input) {
		// TODO Auto-generated method stub
		return null;
	}

	
//	public void setInputType(TypeAndAnnotation ip){
////		
////		JType type = ip.getTypeClass();
////		type.array().
//	}
	
	
}
