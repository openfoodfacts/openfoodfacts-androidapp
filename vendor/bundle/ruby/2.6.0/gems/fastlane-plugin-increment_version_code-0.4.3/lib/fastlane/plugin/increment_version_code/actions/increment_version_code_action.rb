require 'tempfile'
require 'fileutils'

module Fastlane
  module Actions
    class IncrementVersionCodeAction < Action
      def self.run(params)

        version_code = "0"
        new_version_code ||= params[:version_code]

        constant_name ||= params[:ext_constant_name]

        gradle_file_path ||= params[:gradle_file_path]
        if gradle_file_path != nil
            UI.message("The increment_version_code plugin will use gradle file at (#{gradle_file_path})!")
            new_version_code = incrementVersion(gradle_file_path, new_version_code, constant_name)
        else
            app_folder_name ||= params[:app_folder_name]
            UI.message("The get_version_code plugin is looking inside your project folder (#{app_folder_name})!")

            #temp_file = Tempfile.new('fastlaneIncrementVersionCode')
            #foundVersionCode = "false"
            Dir.glob("**/#{app_folder_name}/build.gradle") do |path|
                UI.message(" -> Found a build.gradle file at path: (#{path})!")
                new_version_code = incrementVersion(path, new_version_code, constant_name)
            end

        end

        if new_version_code == -1
            UI.user_error!("Impossible to find the version code with the specified properties 😭")
        else
            # Store the version name in the shared hash
            Actions.lane_context["VERSION_CODE"]=new_version_code
            UI.success("☝️ Version code has been changed to #{new_version_code}")
        end

        return new_version_code
      end

      def self.incrementVersion(path, new_version_code, constant_name)
          if !File.file?(path)
              UI.message(" -> No file exist at path: (#{path})!")
              return -1
          end
          begin
              foundVersionCode = "false"
              temp_file = Tempfile.new('fastlaneIncrementVersionCode')
              File.open(path, 'r') do |file|
                  file.each_line do |line|
                      if line.include? constant_name and foundVersionCode=="false"
                          UI.message(" -> line: (#{line})!")
                        versionComponents = line.strip.split(' ')
                        version_code = versionComponents[versionComponents.length-1].tr("\"","")
                        if new_version_code <= 0
                            new_version_code = version_code.to_i + 1
                        end
                        if !!(version_code =~ /\A[-+]?[0-9]+\z/)
                            line.replace line.sub(version_code, new_version_code.to_s)
                            foundVersionCode = "true"
                        end
                        temp_file.puts line
                      else
                      temp_file.puts line
                   end
              end
              file.close
            end
            temp_file.rewind
            temp_file.close
            FileUtils.mv(temp_file.path, path)
            temp_file.unlink
          end
          if foundVersionCode == "true"
              return new_version_code
          end
          return -1
      end

      def self.description
        "Increment the version code of your android project."
      end

      def self.authors
        ["Jems"]
      end

      def self.available_options
          [
              FastlaneCore::ConfigItem.new(key: :app_folder_name,
                                      env_name: "INCREMENTVERSIONCODE_APP_FOLDER_NAME",
                                   description: "The name of the application source folder in the Android project (default: app)",
                                      optional: true,
                                          type: String,
                                 default_value:"app"),
             FastlaneCore::ConfigItem.new(key: :gradle_file_path,
                                     env_name: "INCREMENTVERSIONCODE_GRADLE_FILE_PATH",
                                  description: "The relative path to the gradle file containing the version code parameter (default:app/build.gradle)",
                                     optional: true,
                                         type: String,
                                default_value: nil),
              FastlaneCore::ConfigItem.new(key: :version_code,
                                      env_name: "INCREMENTVERSIONCODE_VERSION_CODE",
                                   description: "Change to a specific version (optional)",
                                      optional: true,
                                          type: Integer,
                                 default_value: 0),
              FastlaneCore::ConfigItem.new(key: :ext_constant_name,
                                      env_name: "INCREMENTVERSIONCODE_EXT_CONSTANT_NAME",
                                   description: "If the version code is set in an ext constant, specify the constant name (optional)",
                                      optional: true,
                                          type: String,
                                 default_value: "versionCode")
          ]
      end

      def self.output
        [
          ['VERSION_CODE', 'The new version code of the project']
        ]
      end

      def self.is_supported?(platform)
        [:android].include?(platform)
      end
    end
  end
end
