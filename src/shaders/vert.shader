#version 430 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoord;
layout (std430, binding = 0) buffer ModelMatricies {
    mat4 models[];
};

out vec3 ourNormal;
out vec2 texCoord;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main(){
#ifdef INSTANCED
    vec2 asdf = vec2(4);
    mat4 instanceModel = models[gl_InstanceID];
    if (instanceModel[3][3] == 0.0) { 
        instanceModel = mat4(1.0); // Force identity if data is missing
    }
    gl_Position = projection * view * instanceModel * vec4(aPos, 1.0);
#else
    gl_Position = projection * view * model * vec4(aPos, 1.0);
#endif
    ourNormal = aNormal;
    texCoord = aTexCoord;
}