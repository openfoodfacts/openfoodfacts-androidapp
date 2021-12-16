module Fastlane
  module Actions
    class GetVersionCodeAction < Action
      def self.run(params)
        version_code = "0"

        constant_name ||= params[:ext_constant_name]
        gradle_file_path ||= params[:gradle_file_path]
        if gradle_file_path != nil
            UI.message("The get_version_code plugin will use gradle file at (#{gradle_file_path})!")
            version_code = getVersionCode(gradle_file_path, constant_name)
        else
            app_folder_name ||= params[:app_folder_name]
            UI.message("The get_version_code plugin is looking inside your project folder (#{app_folder_name})!")

            #temp_file = Tempfile.new('fastlaneIncrementVersionCode')
            #foundVersionCode = "false"
            Dir.glob("**/#{app_folder_name}/build.gradle") do |path|
                UI.message(" -> Found a build.gradle file at path: (#{path})!")
                version_code = getVersionCode(path, constant_name)
            end
        end

        if version_code == "0"
            UI.user_error!("Impossible to find the version code in the current project folder #{app_folder_name} 😭")
        else
            # Store the version name in the shared hash
            Actions.lane_context["VERSION_CODE"]=version_code
            UI.success("👍 Version name found: #{version_code}")
        end

        return version_code
      end

      def self.getVersionCode(path, constant_name)
          version_code = "0"
          if !File.file?(path)
              UI.message(" -> No file exist at path: (#{path})!")
              return version_code
          end
          begin
              file = File.new(path, "r")
              while (line = file.gets)
                  if line.include? constant_name
                     versionComponents = line.strip.split(' ')
                     version_code = versionComponents[versionComponents.length - 1].tr("\"","")
                     break
                  end
              end
              file.close
          rescue => err
              UI.error("An exception occured while readinf gradle file: #{err}")
              err
          end
          return version_code
      end

      def self.description
        "Get the version code of an Android project. This action will return the version code of your project according to the one set in your build.gradle file"
      end

      def self.authors
        ["Jems"]
      end

      def self.available_options
          [
            FastlaneCore::ConfigItem.new(key: :app_folder_name,
                                    env_name: "GETVERSIONCODE_APP_FOLDER_NAME",
                                 description: "The name of the application source folder in the Android project (default: app)",
                                    optional: true,
                                        type: String,
                               default_value:"app"),
            FastlaneCore::ConfigItem.new(key: :gradle_file_path,
                                    env_name: "GETVERSIONCODE_GRADLE_FILE_PATH",
                                 description: "The relative path to the gradle file containing the version code parameter (default:app/build.gradle)",
                                    optional: true,
                                        type: String,
                               default_value: nil),
             FastlaneCore::ConfigItem.new(key: :ext_constant_name,
                                     env_name: "GETVERSIONCODE_EXT_CONSTANT_NAME",
                                  description: "If the version code is set in an ext constant, specify the constant name (optional)",
                                     optional: true,
                                         type: String,
                                default_value: "versionCode")
          ]
        end

        def self.output
          [
            ['VERSION_CODE', 'The version code of the project']
          ]
        end

        def self.is_supported?(platform)
          [:android].include?(platform)
        end
    end
  end
end
