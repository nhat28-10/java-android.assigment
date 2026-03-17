const fs = require('fs');
const path = 'D:/Desktop/PRM_MOBILE/java-android.assigment/app/src/main/AndroidManifest.xml';

let content = fs.readFileSync(path, 'utf8');
content = content.replace(
    '.manager.AnnotatorAuditDetailActivity" />',
    '.manager.AnnotatorAuditDetailActivity" />\n        <activity android:name=".ui.admin.AdminDashboardActivity" />\n        <activity android:name=".ui.admin.UserManagementActivity" />'
);

fs.writeFileSync(path, content);
console.log('Done!');
