package me.dpohvar.powernbt.api;

import org.bukkit.World;

import java.util.*;

import static me.dpohvar.powernbt.utils.NBTUtils.nbtUtils;

/**
 * Represent net.minecraft.server.NBTTagCompound.
 * Allows you to work with NBTTagCompound as with Map.
 * use java.lang.String as keys,
 * values on get() will be converted to java primitive types if it possible.
 * net.minecraft.server.NBTTagList converted to NBTList
 * net.minecraft.server.NBTTagCompound converted to NBTCompound
 * types allowed to put:
 * - all primitive types (boolean -> NBTTagByte 0 or 1)
 * - Object[] -> NBTTagList
 * - java.util.Collection -> NBTTagList
 * - java.util.Map -> NBTTagCompound
 * arrays, collections and maps must contains only the allowed values
 */
public class NBTCompound implements Map<String,Object> {

    private final Map<String,Object> handleMap;
    private final Object handle;

    /**
     * create NBTCompound by NBTTagCompound
     * @param tag instance of net.minecraft.server.NBTTagCompound
     * @return NBTCompound wrapper
     */
    public static NBTCompound forNBT(Object tag){
        if (tag==null) return null;
        return new NBTCompound(tag);
    }

    /**
     * create NBTCompound by copy of NBTTagCompound
     * @param tag instance of net.minecraft.server.NBTTagCompound
     * @return NBTCompound wrapper
     */
    public static NBTCompound forNBTCopy(Object tag){
        if (tag==null) return null;
        return forNBT(nbtUtils.cloneTag(tag));
    }

    NBTCompound(Object tag){
        assert nbtUtils.getTagType(tag) == 10;
        this.handle = tag;
        this.handleMap = nbtUtils.getHandleMap(tag);
    }

    /**
     * get original NBTTagCompound
     * Be careful!
     * @return NBTTagCompound
     */
    public Object getHandle(){
        return handle;
    }

    /**
     * get copy of original NBTTagCompound.
     * @return NBTTagCompound
     */
    public Object getHandleCopy(){
        return nbtUtils.cloneTag(handle);
    }

    /**
     * get Map stored in original NBTTagCompound.
     * Be careful!
     * @return Map
     */
    public Map<String,Object> getHandleMap(){
        return handleMap;
    }

    /**
     * Create new empty NBTCompound
     */
    public NBTCompound(){
        this(nbtUtils.createTagCompound());
    }

    @Override
    public boolean equals(Object t){
        return t instanceof NBTCompound
                && handle.equals(((NBTCompound) t).handle);
    }

    /**
     * Convert Map to NBTCompound
     * @param values map to convert
     */
    public NBTCompound(Map values){
        this(nbtUtils.createTag(values, (byte) 10));
    }

    @Override
    public NBTCompound clone(){
        return new NBTCompound(nbtUtils.cloneTag(handle));
    }

    @Override
    public int size() {
        return handleMap.size();
    }

    @Override
    public boolean isEmpty() {
        return handleMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return handleMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        Object tag = nbtUtils.createTag(value);
        return handleMap.containsValue(tag);
    }

    @Override
    public Object get(Object key) {
        return nbtUtils.getValue(handleMap.get(key));
    }

    @Override
    public Object put(String key, Object value) {
        if (value==null) return remove(key);
        Object tag = nbtUtils.createTag(value);
        Object oldTag = handleMap.put(key,tag);
        return nbtUtils.getValue(oldTag);
    }

    @Override
    public Object remove(Object key) {
        Object oldTag = handleMap.remove(key);
        return nbtUtils.getValue(oldTag);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        for (Entry<? extends String, ?> e: m.entrySet()) {
            put(e.getKey(),e.getValue());
        }
    }

    @Override
    public void clear() {
        handleMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return handleMap.keySet();
    }

    @Override
    public Collection<Object> values() {
        return new NBTValues(handleMap.values());
    }

    @Override
    public NBTEntrySet entrySet() {
        return new NBTEntrySet(handleMap.entrySet());
    }

    /**
     * Merge this compound with map.
     * merging occurs recursively for inner maps
     * @param map map to merge
     */
    public void merge(Map map) {
        for(Object key: map.keySet()) {
            if (!containsKey(key)) {
                put(key.toString(), map.get(key));
                continue;
            }
            Object val = get(key);
            Object value = map.get(key);
            if (val instanceof NBTCompound && value instanceof Map) {
                ((NBTCompound)val).merge((Map)value);
            } else {
                put(key.toString(),value);
            }
        }
    }

    public class NBTValues extends AbstractCollection<Object>{

        Collection<Object> handle;

        private NBTValues(Collection<Object> values) {
            this.handle = values;
        }

        @Override
        public Iterator<Object> iterator() {
            return new NBTValuesIterator(handle.iterator());
        }

        @Override
        public int size() {
            return handle.size();
        }

        public class NBTValuesIterator implements Iterator<Object>{

            private Iterator<Object> handle;

            private NBTValuesIterator(Iterator<Object> iterator) {
                this.handle = iterator;
            }

            @Override
            public boolean hasNext() {
                return handle.hasNext();
            }

            @Override
            public Object next() {
                return nbtUtils.getValue(handle.next());
            }

            @Override
            public void remove() {
                handle.remove();
            }
        }
    }

    public class NBTEntrySet extends AbstractSet<Entry<String, Object>> {

        private Set<Entry<String, Object>> entries;

        public NBTEntrySet(Set<Entry<String, Object>> entries) {
            this.entries = entries;
        }

        @Override
        public NBTIterator iterator() {
            return new NBTIterator(entries.iterator());
        }

        @Override
        public int size() {
            return entries.size();
        }

        public class NBTIterator implements Iterator<Entry<String, Object>> {

            private Iterator<Entry<String, Object>> iterator;

            private NBTIterator(Iterator<Entry<String, Object>> iterator) {
                this.iterator = iterator;
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public NBTEntry next() {
                return new NBTEntry(iterator.next());
            }

            @Override
            public void remove() {
                iterator.remove();
            }

            public class NBTEntry implements Entry<String, Object>{

                private Entry<String, Object> entry;

                public NBTEntry(Entry<String, Object> entry) {
                    this.entry = entry;
                }

                @Override
                public String getKey() {
                    return entry.getKey();
                }

                @Override
                public Object getValue() {
                    return nbtUtils.getValue(entry.getValue());
                }

                @Override
                public Object setValue(Object value) {
                    if (value==null) {
                        Object val = getValue();
                        remove();
                        return val;
                    } else {
                        Object tag = nbtUtils.createTag(value);
                        Object oldTag = entry.setValue(tag);
                        return nbtUtils.getValue(oldTag);
                    }
                }
            }
        }
    }

    public String toString() {
        NBTEntrySet.NBTIterator i = entrySet().iterator();
        if (!i.hasNext()) return "{}";
        StringBuilder sb = new StringBuilder().append('{');
        for (;;) {
            NBTEntrySet.NBTIterator.NBTEntry e = i.next();
            Object val = e.getValue();
            sb.append(e.getKey()).append('=');
            if (val instanceof byte[]) {
                sb.append( "int[" + ((byte[])val).length + ']');
            } else if (val instanceof int[]) {
                sb.append( "byte[" + ((int[])val).length + ']');
            } else {
                sb.append(val);
            }
            if (!i.hasNext()) return sb.append('}').toString();
            sb.append(", ");
        }
    }

    /**
     * try to get value and convert to boolean
     * @param key key
     * @return value, false by default
     */
    public boolean getBoolean(String key) {
        Object val = get(key);
        if (val instanceof Float) return ((Float)val)!=0.f;
        if (val instanceof Double) return ((Double)val)!=0.d;
        if (val instanceof Number) return ((Number)val).longValue()!=0;
        if (val instanceof String) return ((String)val).isEmpty();
        if (val instanceof int[]) return ((int[])val).length!=0;
        if (val instanceof byte[]) return ((byte[])val).length!=0;
        if (val instanceof Collection) return !((Collection)val).isEmpty();
        if (val instanceof Map) return !((Map)val).isEmpty();
        return false;
    }

    /**
     * try to get byte value or convert to byte
     * @param key key
     * @return value, 0 by default
     */
    public byte getByte(String key) {
        Object val = get(key);
        if (val instanceof Number) return ((Number)val).byteValue();
        if (val instanceof String) try {
            return (byte) Long.parseLong((String)val);
        } catch (Exception e){
            try {
                return (byte) Double.parseDouble((String) val);
            } catch (Exception ignored){
            }
        }
        return 0;
    }

    /**
     * try to get short value or convert to short
     * @param key key
     * @return value, 0 by default
     */
    public short getShort(String key) {
        Object val = get(key);
        if (val instanceof Number) return ((Number)val).shortValue();
        if (val instanceof String) try {
            return (short) Long.parseLong((String)val);
        } catch (Exception e){
            try {
                return (short) Double.parseDouble((String)val);
            } catch (Exception ignored){
            }
        }
        return 0;
    }

    /**
     * try to get int value or convert to int
     * @param key key
     * @return value, 0 by default
     */
    public int getInt(String key) {
        Object val = get(key);
        if (val instanceof Number) return ((Number)val).intValue();
        if (val instanceof String) try {
            return (int) Long.parseLong((String)val);
        } catch (Exception e){
            try {
                return (int) Double.parseDouble((String)val);
            } catch (Exception ignored){
            }
        }
        return 0;
    }

    /**
     * try to get long value or convert to long
     * @param key key
     * @return value, 0 by default
     */
    public long getLong(String key) {
        Object val = get(key);
        if (val instanceof Number) return ((Number)val).longValue();
        if (val instanceof String) try {
            return Long.parseLong((String)val);
        } catch (Exception e){
            try {
                return (long) Double.parseDouble((String)val);
            } catch (Exception ignored){
            }
        }
        return 0;
    }

    /**
     * try to get float value or convert to float
     * @param key key
     * @return value, 0 by default
     */
    public float getFloat(String key) {
        Object val = get(key);
        if (val instanceof Number) return ((Number)val).floatValue();
        if (val instanceof String) try {
            return (float) Double.parseDouble((String)val);
        } catch (Exception ignored){
        }
        return 0;
    }

    /**
     * try to get double value or convert to double
     * @param key key
     * @return value, 0 by default
     */
    public double getDouble(String key) {
        Object val = get(key);
        if (val instanceof Number) return ((Number)val).doubleValue();
        if (val instanceof String) try {
            return Double.parseDouble((String)val);
        } catch (Exception ignored){
        }
        return 0;
    }

    /**
     * try to get string value or convert string
     * @param key key
     * @return value, empty string by default
     */
    public String getString(String key) {
        Object val = get(key);
        if (val == null) return "";
        else return val.toString();
    }

    /**
     * get NBTCompound or create new one
     * Example: new NBTCompound().compound("display").list("Lore").add("lore1")
     * @param key key
     * @return existing or created compound
     */
    public NBTCompound compound(String key) {
        Object val = get(key);
        if (val instanceof NBTCompound) return (NBTCompound) val;
        NBTCompound compound = new NBTCompound();
        put(key,compound);
        return compound;
    }

    /**
     * get NBTList or create new one
     * Example: new NBTCompound().compound("display").list("Lore").add("lore1")
     * @param key key
     * @return existing or created list
     */
    public NBTList list(String key) {
        Object val = get(key);
        if (val instanceof NBTList) return (NBTList) val;
        NBTList list = new NBTList();
        put(key,list);
        return list;
    }

    /**
     * Check if compound contains key with value of specific type
     * @param key key
     * @param type type of value
     * @return true if compound has key with specific value
     */
    public boolean containsKey(String key, Class type){
        Object t = get(key);
        return type.isInstance(key);
    }

    /**
     * Check if compound contains key with value of specific type
     * @param key key
     * @param type byte type of NBT tag
     * @return true if compound has key with specific value
     */
    public boolean containsKey(String key, byte type){
        Object tag = handleMap.get(key);
        return tag != null && nbtUtils.getTagType(tag) == type;
    }

}









