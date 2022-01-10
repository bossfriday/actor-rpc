package cn.bossfridy.rpc.test.actorsystem.modules;

import cn.bossfridy.utils.ProtostuffCodecUtil;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Foo {
    private String id;

    private String name;

    private Integer age;

    private String desc;

    public static void main(String[] args) {
        Foo foo = Foo.builder().id("1").name("foo").age(100).desc("Foo is a fuck oriented object!").build();
        byte[] data = ProtostuffCodecUtil.serialize(foo);
        Foo result = ProtostuffCodecUtil.deserialize(data, Foo.class);
        System.out.println(result.toString());
    }
}
