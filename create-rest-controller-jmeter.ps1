# Script pour créer les fichiers JMeter pour @RestController
$files = @(
    @{Source='read-heavy-scenario.jmx'; Dest='read-heavy-scenario-rest-controller.jmx'; TestName='READ-heavy Scenario - RestController'; TestGroup='READ-heavy Test - RestController'},
    @{Source='join-filter-scenario.jmx'; Dest='join-filter-scenario-rest-controller.jmx'; TestName='JOIN-filter Scenario - RestController'; TestGroup='JOIN-filter Test - RestController'},
    @{Source='mixed-scenario.jmx'; Dest='mixed-scenario-rest-controller.jmx'; TestName='MIXED Scenario - RestController'; TestGroup='MIXED Test - RestController'},
    @{Source='heavy-body-scenario.jmx'; Dest='heavy-body-scenario-rest-controller.jmx'; TestName='HEAVY-body Scenario - RestController'; TestGroup='HEAVY-body Test - RestController'}
)

foreach ($file in $files) {
    $content = Get-Content "jmeter\$($file.Source)" -Raw
    $content = $content -replace '/api/', '/api-rest/'
    $content = $content -replace $file.Source.Split('-')[0] + ' Scenario', $file.TestName
    $content = $content -replace $file.Source.Split('-')[0] + ' Test', $file.TestGroup
    
    # Fix specific replacements
    if ($file.Source -eq 'read-heavy-scenario.jmx') {
        $content = $content -replace 'READ-heavy Scenario', 'READ-heavy Scenario - RestController'
        $content = $content -replace 'READ-heavy Test', 'READ-heavy Test - RestController'
    }
    if ($file.Source -eq 'join-filter-scenario.jmx') {
        $content = $content -replace 'JOIN-filter Scenario', 'JOIN-filter Scenario - RestController'
        $content = $content -replace 'JOIN-filter Test', 'JOIN-filter Test - RestController'
    }
    if ($file.Source -eq 'mixed-scenario.jmx') {
        $content = $content -replace 'MIXED Scenario', 'MIXED Scenario - RestController'
        $content = $content -replace 'MIXED Test', 'MIXED Test - RestController'
    }
    if ($file.Source -eq 'heavy-body-scenario.jmx') {
        $content = $content -replace 'HEAVY-body Scenario', 'HEAVY-body Scenario - RestController'
        $content = $content -replace 'HEAVY-body Test', 'HEAVY-body Test - RestController'
    }
    
    Set-Content "jmeter\$($file.Dest)" -Value $content -NoNewline
    Write-Host "Created: $($file.Dest)"
}

Write-Host "All files created successfully!"


