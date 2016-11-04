package com.TeamNovus.AutoMessage.Util;

import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtil {
	private static final Class INVALID_CLASS = Void.class;
	private static final Method INVALID_METHOD;
	private static final Field INVALID_FIELD;
	private static final Constructor INVALID_CONSTRUCTOR = String.class.getConstructors()[0];
	private static String version;
	private static Class craftPlayerClass;
	private static Method getHandle;
	private static Class entityPlayerClass;
	private static Field playerConnection;
	private static Class playerConnectionClass;
	private static Method sendPacket;
	private static Class chatSerializerClass;
	private static Method fromJson;
	private static Class iChatBaseComponentClass;
	private static Class packetPlayOutChatClass;
	private static Constructor packetPlayOutChatConstructor;
	private static Class packetClass;

	static {
		try {
			INVALID_METHOD = Boolean.class.getDeclaredMethod("parseBoolean", String.class);
		} catch (NoSuchMethodException e) {
			throw new AssertionError(e);
		}
		try {
			INVALID_FIELD = Void.class.getField("TYPE");
		} catch (NoSuchFieldException e) {
			throw new AssertionError(e);
		}
	}

	private ReflectionUtil() {
	}

	public static String getVersion() {
		if (version != null)
			return version;

		return version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
	}

	public static String getNMS(String name) {
		return "net.minecraftserver." + getVersion() + "." + name;
	}

	public static String getOBC(String name) {
		return "org.bukkit.craftbukkit." + getVersion() + "." + name;
	}

	private static Class findClass(String... names) {
		List<ClassNotFoundException> exceptions = new ArrayList<ClassNotFoundException>(names.length);
		for (String name : names) {
			try {
				return Class.forName(name);
			} catch (ClassNotFoundException e) {
				exceptions.add(e);
			}
		}

		for (ClassNotFoundException exception : exceptions) {
			exception.printStackTrace();
		}

		return INVALID_CLASS;
	}


	private static Method findMethod(Class<?> c, String name, Class<?>... params) {
		return findMethod(c, new String[]{name}, params);
	}

	private static Method findMethod(Class<?> c, String[] names, Class<?>... params) {
		if (c == null || c == INVALID_CLASS)
			return null;

		List<NoSuchMethodException> exceptions = new ArrayList<NoSuchMethodException>(names.length);
		for (String name : names) {
			try {
				return c.getMethod(name, params);
			} catch (NoSuchMethodException e) {
				exceptions.add(e);
			}
		}

		for (NoSuchMethodException exception : exceptions) {
			exception.printStackTrace();
		}

		return INVALID_METHOD;
	}

	private static Field findField(Class<?> c, String... names) {
		if (c == null || c == INVALID_CLASS)
			return null;

		List<NoSuchFieldException> exceptions = new ArrayList<NoSuchFieldException>(names.length);
		for (String name : names) {
			try {
				return c.getField(name);
			} catch (NoSuchFieldException e) {
				exceptions.add(e);
			}
		}

		for (NoSuchFieldException exception : exceptions) {
			exception.printStackTrace();
		}

		return INVALID_FIELD;
	}

	public static Class getCraftPlayerClass() {
		if (craftPlayerClass == INVALID_CLASS)
			return null;
		if (craftPlayerClass != null)
			return craftPlayerClass;

		return craftPlayerClass = findClass(getOBC("entity.CraftPlayer"));
	}

	public static Method getGetHandle() {
		if (getHandle == INVALID_METHOD)
			return null;
		if (getHandle != null)
			return getHandle;

		return getHandle = findMethod(getCraftPlayerClass(), "getHandle");
	}

	public static Class getEntityPlayerClass() {
		if (entityPlayerClass == INVALID_CLASS)
			return null;
		if (entityPlayerClass != null)
			return entityPlayerClass;

		return entityPlayerClass = findClass(getNMS("EntityPlayer"), "net.minecraft.entity.player.EntityPlayerMP");
	}

	public static Field getPlayerConnection() {
		if (playerConnection == INVALID_FIELD)
			return null;
		if (playerConnection != null)
			return playerConnection;

		return playerConnection = findField(getEntityPlayerClass(), "playerConnection", "field_71135_a");
	}

	public static Class getPlayerConnectionClass() {
		if (playerConnectionClass == INVALID_CLASS)
			return null;
		if (playerConnectionClass != null)
			return playerConnectionClass;

		return playerConnectionClass = findClass(getNMS("PlayerConnection"), "net.minecraft.network.NetHandlerPlayServer");
	}

	public static Method getSendPacket() {
		if (sendPacket == INVALID_METHOD)
			return null;
		if (sendPacket != null)
			return sendPacket;

		return sendPacket = findMethod(getPlayerConnectionClass(), new String[]{"sendPacket", "func_147359_a"}, getPacketClass());
	}

	public static Class getChatSerializerClass() {
		if (chatSerializerClass == INVALID_CLASS)
			return null;
		if (chatSerializerClass != null)
			return chatSerializerClass;

		return chatSerializerClass = findClass(
				getNMS("ChatSerializer"),
				getNMS("IChatBaseComponent$ChatSerializer"),
				"net.minecraft.util.IChatComponent$Serializer"
		);
	}

	public static Method getFromJson() {
		if (fromJson == INVALID_METHOD)
			return null;
		if (fromJson != null)
			return fromJson;

		return fromJson = findMethod(getChatSerializerClass(), new String[]{"a", "func_150699_a"}, String.class);
	}

	public static Object invoke(Method method, Object owner, Object... params) throws InvocationTargetException, IllegalAccessException {
		if (method == null)
			return null;

		return method.invoke(owner, params);
	}

	public static Class getIChatBaseComponentClass() {
		if (iChatBaseComponentClass == INVALID_CLASS)
			return null;
		if (iChatBaseComponentClass != null)
			return iChatBaseComponentClass;

		return iChatBaseComponentClass = findClass(getNMS("IChatBaseComponent"), "net.minecraft.util.IChatComponent");
	}

	public static Class getPacketPlayOutChatClass() {
		if (packetPlayOutChatClass == INVALID_CLASS)
			return null;
		if (packetPlayOutChatClass != null)
			return packetPlayOutChatClass;

		return packetPlayOutChatClass = findClass(getNMS("PacketPlayOutChat"), "net.minecraft.network.play.server.S02PacketChat");
	}

	public static Constructor getPacketPlayOutChatConstructor() {
		if (packetPlayOutChatConstructor == INVALID_CONSTRUCTOR)
			return null;
		if (packetPlayOutChatConstructor != null)
			return packetPlayOutChatConstructor;

		Class<?> packet = getPacketPlayOutChatClass();
		Class<?> component = getIChatBaseComponentClass();
		if (packet == null || packet == INVALID_CLASS || component == null || component == INVALID_CLASS)
			return null;

		try {
			return packetPlayOutChatConstructor = packet.getConstructor(component);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return packetPlayOutChatConstructor = INVALID_CONSTRUCTOR;
		}
	}

	public static Object construct(Constructor constructor, Object... params) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		if (constructor == null)
			return null;
		return constructor.newInstance(params);
	}

	public static Class getPacketClass() {
		if (packetClass == INVALID_CLASS)
			return null;
		if (packetClass != null)
			return packetClass;

		return packetClass = findClass(getNMS("Packet"), "net.minecraft.network.Packet");
	}

	public static Object cast(Object obj, Class to) {
		return to.cast(obj);
	}

	public static Object get(Field field, Object from) throws IllegalAccessException {
		return field.get(from);
	}
}
