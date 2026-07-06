const fs = require('fs');
const path = require('path');

const feDir = path.join(__dirname, 'Fe/src');
const endpoints = require('./backend_endpoints.json');

// Get all files in frontend
function getAllFiles(dir, fileList = []) {
  const files = fs.readdirSync(dir);
  for (const file of files) {
    const stat = fs.statSync(path.join(dir, file));
    if (stat.isDirectory()) {
      getAllFiles(path.join(dir, file), fileList);
    } else if (file.endsWith('.ts') || file.endsWith('.tsx')) {
      fileList.push(path.join(dir, file));
    }
  }
  return fileList;
}

const tsFiles = getAllFiles(feDir);
let allContent = '';
for (const f of tsFiles) {
  allContent += fs.readFileSync(f, 'utf-8') + '\n';
}

const uncalled = [];

for (const ep of endpoints) {
  let isCalled = false;
  // create a search pattern.
  // e.g. /api/users/{id}/roles -> users.*roles
  // remove leading /api if present, but actually we see classPrefix might just be /users
  let cleanPath = ep.path.replace(/^\/api/, '');
  
  // Replace path variables like {id} with regex wildcard
  const patternStr = cleanPath.split('/').filter(Boolean).map(segment => {
    if (segment.startsWith('{') && segment.endsWith('}')) {
      return '[^/]+';
    }
    return segment;
  }).join('/');

  // If patternStr is empty (e.g. mapping is just "/"), check differently or skip
  if (!patternStr) {
    // maybe it's a base endpoint
    uncalled.push(ep);
    continue;
  }

  // Create a regex to find this path in the frontend code
  // Frontend might use backticks like `/projects/${id}/sprints`
  // We can just check if the static parts are present in the code.
  const staticParts = cleanPath.split('/').filter(Boolean).filter(s => !s.startsWith('{'));
  
  // A naive check: does the file content contain all static parts in proximity?
  // Or just check if the specific string structure exists.
  
  // Let's do a simple regex that matches the segments
  const regex = new RegExp(staticParts.join('.*?'), 'i');
  if (regex.test(allContent)) {
    isCalled = true;
  }

  // Double check with simpler logic if there's only 1 static part
  if (!isCalled && staticParts.length === 1) {
     const rgx2 = new RegExp(`['"\`]\\/?${staticParts[0]}['"\`\\/]`, 'i');
     if (rgx2.test(allContent)) isCalled = true;
  }

  if (!isCalled) {
    uncalled.push(ep);
  }
}

fs.writeFileSync(path.join(__dirname, 'uncalled_endpoints.json'), JSON.stringify(uncalled, null, 2));
console.log('Found', uncalled.length, 'potentially uncalled endpoints.');
