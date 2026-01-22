package dev.latvian.mods.vidlib.util.mutable;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class RefMutableInt extends MutableInt {
	private final IntSupplier getter;
	private final IntConsumer setter;

	public RefMutableInt(IntSupplier getter, IntConsumer setter) {
		this.getter = getter;
		this.setter = setter;
	}

	@Override
	public void add(final int operand) {
		setValue(intValue() + operand);
	}

	@Override
	public void add(final Number operand) {
		setValue(intValue() + operand.intValue());
	}

	@Override
	public int addAndGet(final int operand) {
		setValue(intValue() + operand);
		return intValue();
	}

	@Override
	public int addAndGet(final Number operand) {
		setValue(intValue() + operand.intValue());
		return intValue();
	}

	@Override
	public int compareTo(final MutableInt other) {
		return NumberUtils.compare(intValue(), other.intValue());
	}

	@Override
	public void decrement() {
		setValue(intValue() - 1);
	}

	@Override
	public int decrementAndGet() {
		setValue(intValue() - 1);
		return intValue();
	}

	@Override
	public double doubleValue() {
		return intValue();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof MutableInt) {
			return intValue() == ((MutableInt) obj).intValue();
		}
		return false;
	}

	@Override
	public float floatValue() {
		return intValue();
	}

	@Override
	public int getAndAdd(final int operand) {
		final int last = intValue();
		setValue(last + operand);
		return last;
	}

	@Override
	public int getAndAdd(final Number operand) {
		final int last = intValue();
		setValue(last + operand.intValue());
		return last;
	}

	@Override
	public int getAndDecrement() {
		final int last = intValue();
		setValue(last - 1);
		return last;
	}

	@Override
	public int getAndIncrement() {
		final int last = intValue();
		setValue(last + 1);
		return last;
	}

	@Override
	public Integer getValue() {
		return Integer.valueOf(intValue());
	}

	@Override
	public int hashCode() {
		return intValue();
	}

	@Override
	public void increment() {
		setValue(intValue() + 1);
	}

	@Override
	public int incrementAndGet() {
		setValue(intValue() + 1);
		return intValue();
	}

	@Override
	public int intValue() {
		return getter.getAsInt();
	}

	@Override
	public long longValue() {
		return intValue();
	}

	@Override
	public void setValue(final int value) {
		setter.accept(value);
	}

	@Override
	public void setValue(final Number value) {
		setValue(value.intValue());
	}

	@Override
	public void subtract(final int operand) {
		setValue(intValue() - operand);
	}

	@Override
	public void subtract(final Number operand) {
		setValue(intValue() - operand.intValue());
	}

	@Override
	public Integer toInteger() {
		return Integer.valueOf(intValue());
	}

	@Override
	public String toString() {
		return String.valueOf(intValue());
	}
}
