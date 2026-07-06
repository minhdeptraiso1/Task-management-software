const fs = require('fs');
const path = require('path');

const controllersDir = path.join(__dirname, 'TodoListProject/src/main/java/com/project/taskmanagement/controller');

function parseControllers() {
  const endpoints = [];
  const files = fs.readdirSync(controllersDir).filter(f => f.endsWith('.java'));

  for (const file of files) {
    const content = fs.readFileSync(path.join(controllersDir, file), 'utf-8');
    
    // Find class level RequestMapping
    const classMappingMatch = content.match(/@RequestMapping\(\s*["']([^"']+)["']\s*\)/);
    const classPrefix = classMappingMatch ? classMappingMatch[1] : '';

    // Find all method mappings
    const methodRegex = /@(Get|Post|Put|Delete|Patch)Mapping(?:\(\s*(?:value\s*=\s*)?["']([^"']*)["'][^\)]*\))?/g;
    let match;
    while ((match = methodRegex.exec(content)) !== null) {
      const method = match[1].toUpperCase();
      const methodPath = match[2] || '';
      const fullPath = (classPrefix + methodPath).replace(/\/+/g, '/').replace(/\/$/, '') || '/';
      endpoints.push({ method, path: fullPath, file });
    }
  }
  return endpoints;
}

const endpoints = parseControllers();
fs.writeFileSync(path.join(__dirname, 'backend_endpoints.json'), JSON.stringify(endpoints, null, 2));
console.log('Found', endpoints.length, 'endpoints in backend.');
