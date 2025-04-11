import os
import javalang
from graphviz import Digraph

def parse_java_files(root_dir):
    """
    Recursively parse all Java files in the given directory,
    and extract class declarations and their inheritance relationships.
    """
    classes = {}          # Dictionary to track discovered classes
    extends_edges = []    # List of tuples (child, parent)

    for subdir, _, files in os.walk(root_dir):
        for filename in files:
            if filename.endswith(".java"):
                file_path = os.path.join(subdir, filename)
                try:
                    with open(file_path, "r", encoding="utf-8") as f:
                        source_code = f.read()
                    tree = javalang.parse.parse(source_code)
                except Exception as e:
                    print(f"Skipping {file_path}: {e}")
                    continue

                # Filter for class declarations in the parsed AST
                for _, node in tree.filter(javalang.tree.ClassDeclaration):
                    class_name = node.name
                    classes[class_name] = node
                    if node.extends:
                        # Extract the parent class name. node.extends may be a type.
                        # We take the simple 'name' property here.
                        parent_name = node.extends.name if hasattr(node.extends, "name") else str(node.extends)
                        extends_edges.append((class_name, parent_name))
    
    return classes, extends_edges

def generate_dot(classes, extends_edges, output_filename):
    """
    Generate a class diagram using Graphviz based on the collected class information.
    Nodes represent classes, and edges represent inheritance relationships.
    """
    dot = Digraph(comment="Java Class Diagram")

    # Add each class as a node.
    for class_name in classes:
        dot.node(class_name, class_name)

    # Add edges (inheritance relationships) between classes.
    # The convention here is to draw an arrow from the parent to the child.
    for child, parent in extends_edges:
        dot.edge(parent, child, label="extends")

    # Render the dot file to a PDF (or other format as you like).
    output_path = dot.render(output_filename, view=True)
    print(f"Diagram generated at: {output_path}")

if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description="Generate a class diagram for a Java project")
    parser.add_argument("root_dir", help="Path to the root directory of your Java source code")
    parser.add_argument("output_file", help="Output file name for the generated diagram (without extension)")
    args = parser.parse_args()

    # Parse the Java project and extract classes and inheritance edges.
    classes, extends_edges = parse_java_files(args.root_dir)
    
    # Generate and render the class diagram using Graphviz.
    generate_dot(classes, extends_edges, args.output_file)
