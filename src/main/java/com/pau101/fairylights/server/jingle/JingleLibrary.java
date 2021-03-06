package com.pau101.fairylights.server.jingle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Closeables;
import com.pau101.fairylights.FairyLights;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.RegistryNamespacedDefaultedByKey;

public class JingleLibrary {
	private static final String UNKNOWN_ID = "";

	private static final RegistryNamespacedDefaultedByKey<String, JingleLibrary> REGISTRY = new RegistryNamespacedDefaultedByKey<>(UNKNOWN_ID);

	private static final JingleLibrary UNKNOWN = register(new JingleLibrary(UNKNOWN_ID) {
		@Override
		public void load() {}
	});

	private static final int MAX_RANGE = 25;

	private static int nextFeatureId;

	private final String name;

	private final Map<String, Jingle> jingles = new HashMap<>();

	private final Multimap<Integer, Jingle> jinglesWithinRange = ArrayListMultimap.create();

	private final Map<Integer, Integer> rangeWeights = new HashMap<>();

	private JingleLibrary(String name) {
		this.name = name;
	}

	public int getId() {
		return REGISTRY.getIDForObject(this);
	}

	public boolean contains(String id) {
		return jingles.containsKey(id);
	}

	@Nullable
	public Jingle get(String id) {
		return jingles.get(id);
	}

	private void put(String id, Jingle jingle) {
		jingles.put(id, jingle);
		for (int range = jingle.getRange(); range <= MAX_RANGE; range++) {
			jinglesWithinRange.put(range, jingle);
			rangeWeights.merge(range, jingle.getRange(), Math::addExact);
		}
	}

	@Nullable
	public synchronized Jingle getRandom(Random rng, int range) {
		int fitRange = Math.min(range, MAX_RANGE);
		Collection<Jingle> jingles = jinglesWithinRange.get(fitRange);
		if (jingles.isEmpty()) {
			return null;
		}
		float choice = rng.nextFloat() * rangeWeights.get(fitRange);
		for (Jingle jingle : jingles) {
			choice -= jingle.getRange();
			if (choice <= 0) {
				return jingle;
			}
		}
		return null;
	}

	public void load() { 
		InputStream in = null;
		try {
			in = MinecraftServer.class.getResourceAsStream("/assets/" + FairyLights.ID + "/jingles/" + name + ".dat");
			deserialize(CompressedStreamTools.readCompressed(in));
		} catch (IOException e) {
			Throwables.propagate(e);
		} finally {
			Closeables.closeQuietly(in);
		}
	}

	private synchronized void deserialize(NBTTagCompound library) {
		jingles.clear();
		jinglesWithinRange.clear();
		rangeWeights.clear();
		for (String id : library.getKeySet()) {
			NBTTagCompound jingleCompound = library.getCompoundTag(id);
			Jingle jingle = Jingle.from(jingleCompound);
			if (jingle.isValid() && !contains(jingle.getId())) {
				put(jingle.getId(), jingle);
			}
		}
	}

	public static JingleLibrary create(String name) {
		return register(new JingleLibrary(name));
	}

	private static JingleLibrary register(JingleLibrary library) {
		REGISTRY.register(nextFeatureId++, library.name, library);
		return library;
	}

	public static JingleLibrary fromId(int id) {
		return REGISTRY.getObjectById(id);
	}

	public static void loadAll() {
		Iterator<JingleLibrary> iter = REGISTRY.iterator();
		while (iter.hasNext()) {
			iter.next().load();
		}
	}
}
