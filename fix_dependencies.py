import os

modules_deps = {
    "ui": '    implementation(Libs.composeUi)\n    implementation(Libs.composeMaterial3)',
    "ai": '    implementation(Libs.gemini)',
    "watch": '    implementation(Libs.coilCompose)\n    implementation(Libs.composeUi)\n    implementation(Libs.composeMaterial3)',
    "health": '    implementation(Libs.healthConnect)',
    "settings": '    implementation(Libs.dataStore)',
    "app": '    implementation(project(":ui"))\n    implementation(project(":ai"))\n    implementation(project(":watch"))\n    implementation(project(":health"))\n    implementation(project(":settings"))\n    implementation(project(":bluetooth"))\n    implementation(project(":notifications"))\n    implementation(project(":sport"))'
}

def update_build_gradle(mod, extra_deps):
    path = f"{mod}/build.gradle.kts"
    if not os.path.exists(path):
        return
    with open(path, "r") as f:
        content = f.read()

    if "dependencies {" in content:
        # Insert extra dependencies
        content = content.replace("dependencies {", f"dependencies {{\n{extra_deps}")
        with open(path, "w") as f:
            f.write(content)

for mod, deps in modules_deps.items():
    update_build_gradle(mod, deps)
