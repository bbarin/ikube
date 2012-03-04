package ikube.data;

import ikube.database.IDataBase;

/**
 * This class will generate an object graph based on the entity and the type. Note that the many to many type of reference is not
 * implemented, it will cause infinite recursion. This can be implemented however it is 17:05 on a Saturday and I just don't feel like it at
 * the moment, too much heavy lifting.
 * 
 * Also bi-directional is not implemented, also infinite recursion.
 * 
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class DataGeneratorFour extends ADataGenerator {

	protected int iterations;
	protected Class<?>[] classes;

	public DataGeneratorFour(IDataBase entityManager, int iterations, Class<?>... classes) {
		super(entityManager);
		this.iterations = iterations;
		this.classes = classes;
	}

	@Override
	public void generate() throws Exception {
		for (int i = 0; i < iterations; i++) {
			for (Class<?> klass : classes) {
				Object entity = createInstance(klass);
				dataBase.persist(entity);
			}
		}
	}

}